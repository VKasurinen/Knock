package com.vkasurinen.knock.chat.domain.models

data class AttachmentUploadUrl(
    val uploadUrl: String,
    val publicUrl: String,
    val headers: Map<String, String>
)
