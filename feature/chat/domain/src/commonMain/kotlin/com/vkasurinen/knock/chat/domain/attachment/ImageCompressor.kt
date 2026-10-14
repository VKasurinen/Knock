package com.vkasurinen.knock.chat.domain.attachment

import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.Result

interface ImageCompressor {
    /**
     * Downscales and re-encodes the image as JPEG and stores it as a local file.
     */
    suspend fun compress(
        bytes: ByteArray,
        originalFileName: String?
    ): Result<OutgoingAttachment, DataError.Local>
}
