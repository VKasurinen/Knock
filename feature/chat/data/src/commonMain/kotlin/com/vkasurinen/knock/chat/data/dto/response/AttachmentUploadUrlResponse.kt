package com.vkasurinen.knock.chat.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AttachmentUploadUrlResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String> = emptyMap(),
    val expiresAt: String? = null
)
