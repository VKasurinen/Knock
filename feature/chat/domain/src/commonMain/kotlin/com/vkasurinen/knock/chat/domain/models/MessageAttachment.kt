package com.vkasurinen.knock.chat.domain.models

enum class AttachmentType {
    IMAGE,
    AUDIO
}

data class MessageAttachment(
    /** Server id, null until the message has been echoed back by the server */
    val id: String?,
    val type: AttachmentType,
    /** Public URL, null until the file has been uploaded */
    val url: String?,
    /** Local copy of the file, only known on the device that sent it */
    val localPath: String?,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)
