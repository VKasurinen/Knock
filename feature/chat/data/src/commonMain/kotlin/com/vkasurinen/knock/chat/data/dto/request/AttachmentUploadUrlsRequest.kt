package com.vkasurinen.knock.chat.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AttachmentUploadUrlsRequest(
    val attachments: List<Attachment>
) {
    @Serializable
    data class Attachment(
        val mimeType: String,
        val fileName: String
    )
}
