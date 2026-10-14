package com.vkasurinen.knock.chat.data.audio

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.vkasurinen.knock.chat.domain.audio.AudioPlayer
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Controls the player in [PlaybackService]. MediaController has to be used on the main thread,
 * so every call is dispatched there.
 */
class AndroidAudioPlayer(
    private val context: Context,
    private val applicationScope: CoroutineScope,
    private val logger: KnockLogger
): AudioPlayer {

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var controller: MediaController? = null
    private var positionJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
            if(isPlaying) startPositionUpdates() else positionJob?.cancel()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if(playbackState == Player.STATE_ENDED) {
                // Rewind, so the next tap plays the message from the start
                controller?.run {
                    pause()
                    seekTo(0)
                }
            }
            updateState()
        }
    }

    override fun play(id: String, source: String, durationMs: Long?, startPositionMs: Long?) {
        applicationScope.launch(Dispatchers.Main) {
            val controller = getController() ?: return@launch
            val isCurrentItem = _playbackState.value.id == id && controller.mediaItemCount > 0

            if(!isCurrentItem) {
                val mediaItem = MediaItem.Builder()
                    .setMediaId(id)
                    .setUri(source)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("Voice message")
                            .build()
                    )
                    .build()
                controller.setMediaItem(mediaItem, startPositionMs ?: 0L)
                controller.prepare()
                _playbackState.value = PlaybackState(
                    id = id,
                    durationMs = durationMs ?: 0L,
                    positionMs = startPositionMs ?: 0L
                )
            } else if(startPositionMs != null) {
                controller.seekTo(startPositionMs)
            }
            controller.play()
        }
    }

    override fun pause() {
        applicationScope.launch(Dispatchers.Main) {
            controller?.pause()
        }
    }

    override fun seekTo(positionMs: Long) {
        applicationScope.launch(Dispatchers.Main) {
            controller?.seekTo(positionMs)
            updateState()
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = applicationScope.launch(Dispatchers.Main) {
            while(isActive) {
                updateState()
                delay(PLAYBACK_POLL_INTERVAL_MS)
            }
        }
    }

    private fun updateState() {
        val controller = controller ?: return
        _playbackState.update { current ->
            val playerDuration = controller.duration.takeIf { it > 0 }
            current.copy(
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition.coerceAtLeast(0L),
                durationMs = playerDuration ?: current.durationMs
            )
        }
    }

    private suspend fun getController(): MediaController? {
        controller?.let { return it }

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        val newController = suspendCancellableCoroutine<MediaController?> { continuation ->
            future.addListener(
                {
                    val result = try {
                        future.get()
                    } catch(e: Exception) {
                        logger.error("Failed to connect to playback service", e)
                        null
                    }
                    continuation.resume(result)
                },
                ContextCompat.getMainExecutor(context)
            )
            continuation.invokeOnCancellation { future.cancel(true) }
        } ?: return null

        newController.addListener(playerListener)
        controller = newController
        return newController
    }
}
