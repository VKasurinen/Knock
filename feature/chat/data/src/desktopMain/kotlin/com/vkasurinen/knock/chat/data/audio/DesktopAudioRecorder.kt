package com.vkasurinen.knock.chat.data.audio

import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.domain.audio.AudioRecorder
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine
import kotlin.math.abs
import kotlin.time.TimeSource

/**
 * Captures the microphone as PCM with Java Sound and streams it into FFmpeg, which encodes it
 * into the same AAC/MPEG-4 format that Android and iOS record.
 */
class DesktopAudioRecorder(
    private val fileStorage: AttachmentFileStorage,
    private val logger: KnockLogger
): AudioRecorder {

    private var recording: Recording? = null

    override fun start(): EmptyResult<DataError.Local> {
        cancel()

        val path = fileStorage.newFilePath(VOICE_MESSAGE_EXTENSION)
        var line: TargetDataLine? = null
        var encoder: Process? = null

        return try {
            line = AudioSystem.getTargetDataLine(CAPTURE_FORMAT).apply {
                open(CAPTURE_FORMAT)
            }
            encoder = Ffmpeg.startAacEncoder(SAMPLE_RATE, path)
            recording = Recording(line, encoder, path).also { it.start() }
            Result.Success(Unit)
        } catch(e: Exception) {
            logger.error("Failed to start recording", e)
            line?.close()
            encoder?.destroyForcibly()
            File(path).delete()
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }

    override fun currentAmplitude(): Float {
        return recording?.takePeakAmplitude() ?: 0f
    }

    override suspend fun stop(): Result<OutgoingAttachment, DataError.Local> {
        val recording = recording ?: return Result.Failure(DataError.Local.NOT_FOUND)
        this.recording = null
        val durationMs = recording.elapsedMs()

        val isEncoded = withContext(Dispatchers.IO) {
            recording.finish()
        }

        val file = File(recording.path)
        if(!isEncoded || !file.exists() || file.length() == 0L) {
            logger.warn("Recording stopped without data")
            file.delete()
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        return Result.Success(
            OutgoingAttachment(
                type = AttachmentType.AUDIO,
                localPath = recording.path,
                fileName = voiceMessageFileName(),
                mimeType = VOICE_MESSAGE_MIME_TYPE,
                sizeBytes = file.length(),
                durationMs = durationMs.coerceAtMost(MAX_RECORDING_DURATION_MS)
            )
        )
    }

    override fun cancel() {
        recording?.discard()
        recording = null
    }

    private inner class Recording(
        private val line: TargetDataLine,
        private val encoder: Process,
        val path: String
    ) {
        private val startMark = TimeSource.Monotonic.markNow()

        @Volatile
        private var isRunning = true

        @Volatile
        private var isDiscarded = false

        // Loudest sample since the last currentAmplitude() call, like MediaRecorder.getMaxAmplitude()
        @Volatile
        private var peakAmplitude = 0

        private val captureThread = Thread(::capture, "voice-recorder").apply {
            isDaemon = true
        }

        fun start() {
            line.start()
            captureThread.start()
        }

        fun elapsedMs(): Long = startMark.elapsedNow().inWholeMilliseconds

        fun takePeakAmplitude(): Float {
            val peak = peakAmplitude
            peakAmplitude = 0
            return (peak / 32767f).coerceIn(0f, 1f)
        }

        /**
         * Stops capturing and waits for FFmpeg to write the file. Returns false if encoding failed.
         */
        fun finish(): Boolean {
            stopCapturing()
            captureThread.join()

            val hasExited = encoder.waitFor(ENCODER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if(!hasExited) {
                encoder.destroyForcibly()
                logger.error("FFmpeg didn't finish encoding the voice message in time")
                return false
            }
            if(encoder.exitValue() != 0) {
                val error = encoder.errorStream.bufferedReader().readText()
                logger.error("FFmpeg failed to encode the voice message: $error")
                return false
            }
            return true
        }

        fun discard() {
            isDiscarded = true
            stopCapturing()
        }

        private fun stopCapturing() {
            isRunning = false
            line.stop()
            line.close()
        }

        private fun capture() {
            val buffer = ByteArray(CAPTURE_BUFFER_SIZE)
            try {
                encoder.outputStream.use { output ->
                    while(isRunning && elapsedMs() < MAX_RECORDING_DURATION_MS) {
                        val count = line.read(buffer, 0, buffer.size)
                        if(count <= 0) {
                            continue
                        }
                        updatePeakAmplitude(buffer, count)
                        output.write(buffer, 0, count)
                    }
                }
            } catch(e: Exception) {
                // FFmpeg exiting closes the pipe, finish() reports that through the exit code
                if(isRunning) {
                    logger.error("Voice recording was interrupted", e)
                }
            } finally {
                line.close()
                if(isDiscarded) {
                    encoder.destroyForcibly()
                    encoder.waitFor(ENCODER_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    File(path).delete()
                }
            }
        }

        private fun updatePeakAmplitude(buffer: ByteArray, count: Int) {
            var peak = peakAmplitude
            // 16 bit little endian samples
            for(i in 0 until count - 1 step 2) {
                val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                peak = maxOf(peak, abs(sample.toShort().toInt()))
            }
            peakAmplitude = peak
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val CAPTURE_BUFFER_SIZE = 4_096
        const val ENCODER_TIMEOUT_SECONDS = 30L

        val CAPTURE_FORMAT = AudioFormat(
            SAMPLE_RATE.toFloat(),
            16,
            1,
            true,
            false
        )
    }
}
