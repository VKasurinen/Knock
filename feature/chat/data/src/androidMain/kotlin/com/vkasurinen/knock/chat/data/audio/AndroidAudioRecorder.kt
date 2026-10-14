package com.vkasurinen.knock.chat.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.domain.audio.AudioRecorder
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import java.io.File
import kotlin.time.TimeSource

class AndroidAudioRecorder(
    private val context: Context,
    private val fileStorage: AttachmentFileStorage,
    private val logger: KnockLogger
): AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var outputPath: String? = null
    private var startMark: TimeSource.Monotonic.ValueTimeMark? = null

    override fun start(): EmptyResult<DataError.Local> {
        cancel()

        val path = fileStorage.newFilePath(VOICE_MESSAGE_EXTENSION)
        val mediaRecorder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        return try {
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44_100)
                setAudioEncodingBitRate(64_000)
                setAudioChannels(1)
                setMaxDuration(MAX_RECORDING_DURATION_MS.toInt())
                setOutputFile(path)
                prepare()
                start()
            }
            recorder = mediaRecorder
            outputPath = path
            startMark = TimeSource.Monotonic.markNow()
            Result.Success(Unit)
        } catch(e: Exception) {
            logger.error("Failed to start recording", e)
            mediaRecorder.release()
            File(path).delete()
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }

    override fun currentAmplitude(): Float {
        val maxAmplitude = try {
            recorder?.maxAmplitude ?: 0
        } catch(e: IllegalStateException) {
            0
        }
        // maxAmplitude is 0..32767
        return (maxAmplitude / 32767f).coerceIn(0f, 1f)
    }

    override suspend fun stop(): Result<OutgoingAttachment, DataError.Local> {
        val mediaRecorder = recorder ?: return Result.Failure(DataError.Local.NOT_FOUND)
        val path = outputPath ?: return Result.Failure(DataError.Local.NOT_FOUND)
        val durationMs = startMark?.elapsedNow()?.inWholeMilliseconds ?: 0L

        val isStopped = try {
            mediaRecorder.stop()
            true
        } catch(e: RuntimeException) {
            // Thrown when stopped before any audio was recorded
            logger.warn("Recording stopped without data")
            false
        } finally {
            mediaRecorder.release()
            reset()
        }

        val file = File(path)
        if(!isStopped || !file.exists() || file.length() == 0L) {
            file.delete()
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        return Result.Success(
            OutgoingAttachment(
                type = AttachmentType.AUDIO,
                localPath = path,
                fileName = voiceMessageFileName(),
                mimeType = VOICE_MESSAGE_MIME_TYPE,
                sizeBytes = file.length(),
                durationMs = durationMs.coerceAtMost(MAX_RECORDING_DURATION_MS)
            )
        )
    }

    override fun cancel() {
        val mediaRecorder = recorder ?: return
        try {
            mediaRecorder.stop()
        } catch(_: RuntimeException) {
        }
        mediaRecorder.release()
        outputPath?.let { File(it).delete() }
        reset()
    }

    private fun reset() {
        recorder = null
        outputPath = null
        startMark = null
    }
}
