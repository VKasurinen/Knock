package com.vkasurinen.knock.chat.data.audio

import com.vkasurinen.knock.chat.domain.audio.AudioPlayer
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.core.data.util.appDataDirectory
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

/**
 * Decodes the AAC voice message to PCM with FFmpeg and plays it from memory with a Java Sound
 * Clip, which supports pausing and seeking. Downloaded voice messages are cached on disk.
 */
class DesktopAudioPlayer(
    private val storageHttpClient: HttpClient,
    private val applicationScope: CoroutineScope,
    private val logger: KnockLogger
): AudioPlayer {

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // Runs every call one after another, so a clip is never used while it's being replaced
    @OptIn(ExperimentalCoroutinesApi::class)
    private val playerDispatcher = Dispatchers.IO.limitedParallelism(1)

    private var clip: Clip? = null
    private var positionJob: Job? = null

    private val cacheDirectory: File
        get() = File(appDataDirectory, "voice_messages").apply { mkdirs() }

    override fun play(id: String, source: String, durationMs: Long?, startPositionMs: Long?) {
        applicationScope.launch(playerDispatcher) {
            val isCurrentItem = _playbackState.value.id == id && clip != null

            if(!isCurrentItem) {
                releaseClip()
                _playbackState.value = PlaybackState(
                    id = id,
                    durationMs = durationMs ?: 0L,
                    positionMs = startPositionMs ?: 0L
                )
                clip = loadClip(source) ?: return@launch
                clip?.setPositionMs(startPositionMs ?: 0L)
            } else if(startPositionMs != null) {
                clip?.setPositionMs(startPositionMs)
            }

            clip?.start()
            updateState()
        }
    }

    override fun pause() {
        applicationScope.launch(playerDispatcher) {
            clip?.stop()
            updateState()
        }
    }

    override fun seekTo(positionMs: Long) {
        applicationScope.launch(playerDispatcher) {
            clip?.setPositionMs(positionMs)
            updateState()
        }
    }

    private suspend fun loadClip(source: String): Clip? {
        return try {
            val file = localFileFor(source) ?: return null
            val pcm = decodeToPcm(file) ?: return null
            val frameCount = pcm.size.toLong() / PLAYBACK_FORMAT.frameSize
            AudioSystem.getClip().apply {
                open(AudioInputStream(ByteArrayInputStream(pcm), PLAYBACK_FORMAT, frameCount))
                addLineListener(::onLineEvent)
            }
        } catch(e: CancellationException) {
            throw e
        } catch(e: Exception) {
            logger.error("Failed to load voice message", e)
            null
        }
    }

    private suspend fun localFileFor(source: String): File? {
        if(source.startsWith("file://")) {
            return File(source.removePrefix("file://").removeWindowsDriveSlash())
        }

        // The query of a signed URL can change, the path identifies the file
        val cachedFile = File(cacheDirectory, "${sha256(source.substringBefore('?'))}.$VOICE_MESSAGE_EXTENSION")
        if(cachedFile.exists()) {
            return cachedFile
        }

        val response = storageHttpClient.get(source)
        if(!response.status.isSuccess()) {
            logger.error("Failed to download voice message: ${response.status}")
            return null
        }
        val tempFile = File(cacheDirectory, "${cachedFile.name}.tmp")
        tempFile.writeBytes(response.bodyAsBytes())
        tempFile.renameTo(cachedFile)
        return cachedFile
    }

    private fun decodeToPcm(file: File): ByteArray? {
        val decoder = Ffmpeg.startPcmDecoder(file.absolutePath, SAMPLE_RATE)
        val pcm = decoder.inputStream.use { it.readBytes() }
        decoder.waitFor(DECODER_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if(decoder.isAlive || decoder.exitValue() != 0 || pcm.isEmpty()) {
            val error = decoder.errorStream.bufferedReader().readText()
            decoder.destroyForcibly()
            logger.error("FFmpeg failed to decode the voice message: $error")
            return null
        }
        return pcm
    }

    private fun onLineEvent(event: LineEvent) {
        if(event.type != LineEvent.Type.START && event.type != LineEvent.Type.STOP) {
            return
        }
        applicationScope.launch(playerDispatcher) {
            val clip = clip ?: return@launch
            if(event.line != clip) {
                return@launch
            }
            if(event.type == LineEvent.Type.STOP && clip.framePosition >= clip.frameLength) {
                // Rewind, so the next tap plays the message from the start
                clip.framePosition = 0
            }
            updateState()
            if(event.type == LineEvent.Type.START) startPositionUpdates() else positionJob?.cancel()
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = applicationScope.launch(playerDispatcher) {
            while(isActive) {
                updateState()
                delay(PLAYBACK_POLL_INTERVAL_MS)
            }
        }
    }

    private fun updateState() {
        val clip = clip ?: return
        _playbackState.update { current ->
            current.copy(
                isPlaying = clip.isRunning,
                positionMs = clip.microsecondPosition / 1_000,
                durationMs = clip.microsecondLength / 1_000
            )
        }
    }

    private fun releaseClip() {
        positionJob?.cancel()
        clip?.run {
            stop()
            close()
        }
        clip = null
    }

    private fun Clip.setPositionMs(positionMs: Long) {
        microsecondPosition = (positionMs * 1_000).coerceIn(0L, microsecondLength)
    }

    /** "file:///C:/Users/..." on Windows */
    private fun String.removeWindowsDriveSlash(): String {
        return if(length > 2 && this[0] == '/' && this[2] == ':') substring(1) else this
    }

    private fun sha256(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.encodeToByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private companion object {
        // Plenty for speech and keeps a 10 minute message at about 29 MB in memory
        const val SAMPLE_RATE = 24_000
        const val DECODER_TIMEOUT_SECONDS = 30L

        val PLAYBACK_FORMAT = AudioFormat(
            SAMPLE_RATE.toFloat(),
            16,
            1,
            true,
            false
        )
    }
}
