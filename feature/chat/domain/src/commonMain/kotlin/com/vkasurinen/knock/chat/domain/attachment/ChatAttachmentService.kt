package com.vkasurinen.knock.chat.domain.attachment

import com.vkasurinen.knock.chat.domain.models.AttachmentUploadUrl
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result

interface ChatAttachmentService {
    /**
     * Returns one upload URL per file, in the same order as [files].
     */
    suspend fun getUploadUrls(
        chatId: String,
        files: List<AttachmentFile>
    ): Result<List<AttachmentUploadUrl>, DataError.Remote>

    suspend fun upload(
        uploadUrl: AttachmentUploadUrl,
        bytes: ByteArray,
        mimeType: String,
        onProgress: (bytesSent: Long) -> Unit
    ): EmptyResult<DataError.Remote>

    data class AttachmentFile(
        val mimeType: String,
        val fileName: String
    )
}
