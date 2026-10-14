@file:OptIn(ExperimentalForeignApi::class)

package com.vkasurinen.knock.chat.data.audio

import com.vkasurinen.knock.chat.domain.audio.AudioPlayer
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import kotlinx.cinterop.ExperimentalForeignApi
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
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol

/**
 * Background playback needs the "audio" background mode in Info.plist and the playback
 * audio session category.
 */
class IosAudioPlayer(
    private val applicationScope: CoroutineScope
): AudioPlayer {

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var player: AVPlayer? = null
    private var endObserver: NSObjectProtocol? = null
    private var positionJob: Job? = null

    override fun play(id: String, source: String, durationMs: Long?, startPositionMs: Long?) {
        applicationScope.launch(Dispatchers.Main) {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)

            val isCurrentItem = _playbackState.value.id == id && player != null
            if(!isCurrentItem) {
                load(id, source, durationMs)
            }
            val currentPlayer = player ?: return@launch
            startPositionMs?.let { seek(currentPlayer, it) }
            currentPlayer.play()
            _playbackState.update { it.copy(isPlaying = true) }
            startPositionUpdates()
        }
    }

    override fun pause() {
        applicationScope.launch(Dispatchers.Main) {
            player?.pause()
            positionJob?.cancel()
            updateState(isPlaying = false)
        }
    }

    override fun seekTo(positionMs: Long) {
        applicationScope.launch(Dispatchers.Main) {
            val currentPlayer = player ?: return@launch
            seek(currentPlayer, positionMs)
            _playbackState.update { it.copy(positionMs = positionMs) }
        }
    }

    private fun load(id: String, source: String, durationMs: Long?) {
        player?.pause()
        endObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }

        val url = if(source.startsWith("file://")) {
            NSURL.fileURLWithPath(source.removePrefix("file://"))
        } else {
            NSURL.URLWithString(source)
        } ?: return

        val item = AVPlayerItem(uRL = url)
        player = AVPlayer(playerItem = item)
        endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            // Rewind, so the next tap plays the message from the start
            player?.let { seek(it, 0L) }
            positionJob?.cancel()
            _playbackState.update { it.copy(isPlaying = false, positionMs = 0L) }
        }

        _playbackState.value = PlaybackState(
            id = id,
            durationMs = durationMs ?: 0L
        )
    }

    private fun seek(player: AVPlayer, positionMs: Long) {
        player.seekToTime(CMTimeMakeWithSeconds(positionMs / 1000.0, 1000))
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = applicationScope.launch(Dispatchers.Main) {
            while(isActive) {
                updateState(isPlaying = (player?.rate ?: 0f) > 0f)
                delay(PLAYBACK_POLL_INTERVAL_MS)
            }
        }
    }

    private fun updateState(isPlaying: Boolean) {
        val currentPlayer = player ?: return
        val positionSeconds = CMTimeGetSeconds(currentPlayer.currentTime())
        val durationSeconds = currentPlayer.currentItem?.let { CMTimeGetSeconds(it.duration) }

        _playbackState.update { current ->
            current.copy(
                isPlaying = isPlaying,
                positionMs = if(positionSeconds.isNaN()) current.positionMs else (positionSeconds * 1000).toLong(),
                durationMs = durationSeconds
                    ?.takeIf { !it.isNaN() && it > 0 }
                    ?.let { (it * 1000).toLong() }
                    ?: current.durationMs
            )
        }
    }
}
