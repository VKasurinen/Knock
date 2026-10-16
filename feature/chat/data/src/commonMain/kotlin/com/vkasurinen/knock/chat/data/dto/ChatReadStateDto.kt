package com.vkasurinen.knock.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatReadStateDto(
    val userId: String,
    val lastDeliveredAt: String? = null,
    val lastReadAt: String? = null
)
