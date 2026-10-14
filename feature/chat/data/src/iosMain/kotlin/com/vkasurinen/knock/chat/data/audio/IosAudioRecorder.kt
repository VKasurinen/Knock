@file:OptIn(ExperimentalForeignApi::class)

package com.vkasurinen.knock.chat.data.audio

import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.domain.audio.AudioRecorder
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import kotlin.math.pow

class IosAudioRecorder(
    private val fileStorage: AttachmentFileStorage,
    private val logger: KnockLogger
): AudioRecorder {

    private var recorder: AVAudioRecorder? = null
    private var outputPath: String? = null

    override fun start(): EmptyResult<DataError.Local> {
        cancel()

        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            withOptions = AVAudioSessionCategoryOptionDefaultToSpeaker,
            error = null
        )
        session.setActive(true, error = null)

        val path = fileStorage.newFilePath(VOICE_MESSAGE_EXTENSION)
        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to NSNumber(unsignedInt = kAudioFormatMPEG4AAC),
            AVSampleRateKey to NSNumber(double = 44_100.0),
            AVNumberOfChannelsKey to NSNumber(int = 1),
            AVEncoderAudioQualityKey to NSNumber(long = AVAudioQualityHigh)
        )
        val audioRecorder = AVAudioRecorder(
            uRL = NSURL.fileURLWithPath(path),
            settings = settings,
            error = null
        )
        audioRecorder.meteringEnabled = true

        val isRecording = audioRecorder.recordForDuration(MAX_RECORDING_DURATION_MS / 1000.0)
        if(!isRecording) {
            logger.error("Failed to start recording")
            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        recorder = audioRecorder
        outputPath = path
        return Result.Success(Unit)
    }

    override fun currentAmplitude(): Float {
        val audioRecorder = recorder ?: return 0f
        audioRecorder.updateMeters()
        // Decibels from -160 (silence) to 0 (max), converted to a linear 0..1 level
        val decibels = audioRecorder.averagePowerForChannel(0u)
        return 10.0.pow(decibels / 20.0).toFloat().coerceIn(0f, 1f)
    }

    override suspend fun stop(): Result<OutgoingAttachment, DataError.Local> {
        val audioRecorder = recorder ?: return Result.Failure(DataError.Local.NOT_FOUND)
        val path = outputPath ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val durationMs = (audioRecorder.currentTime * 1000).toLong()
        audioRecorder.stop()
        reset()

        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
        val sizeBytes = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
        if(sizeBytes == 0L) {
            NSFileManager.defaultManager.removeItemAtPath(path, error = null)
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        return Result.Success(
            OutgoingAttachment(
                type = AttachmentType.AUDIO,
                localPath = path,
                fileName = voiceMessageFileName(),
                mimeType = VOICE_MESSAGE_MIME_TYPE,
                sizeBytes = sizeBytes,
                durationMs = durationMs.coerceAtMost(MAX_RECORDING_DURATION_MS)
            )
        )
    }

    override fun cancel() {
        val audioRecorder = recorder ?: return
        audioRecorder.stop()
        audioRecorder.deleteRecording()
        reset()
    }

    private fun reset() {
        recorder = null
        outputPath = null
    }
}
