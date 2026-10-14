package com.vkasurinen.knock.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageAttachmentDto(
    val id: String,
    val type: String,
    val url: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)
