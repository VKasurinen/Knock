package com.vkasurinen.knock.chat.domain.models

/**
 * A processed file that is ready to be uploaded and sent with a message.
 */
data class OutgoingAttachment(
    val type: AttachmentType,
    val localPath: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)
