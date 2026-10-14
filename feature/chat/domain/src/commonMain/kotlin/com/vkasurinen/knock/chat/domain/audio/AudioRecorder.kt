package com.vkasurinen.knock.chat.domain.audio

import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result

interface AudioRecorder {
    /**
     * Starts recording into a new local file. Requires the microphone permission.
     */
    fun start(): EmptyResult<DataError.Local>

    /**
     * Current input level, from 0 (silence) to 1 (loud). Meant to be polled while recording.
     */
    fun currentAmplitude(): Float

    /**
     * Stops recording and returns the voice message, ready to be sent.
     */
    suspend fun stop(): Result<OutgoingAttachment, DataError.Local>

    /**
     * Stops recording and throws the recording away.
     */
    fun cancel()
}
