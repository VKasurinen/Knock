package com.vkasurinen.knock.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatParticipantProfileDto(
    val userId: String,
    val username: String,
    val profilePictureUrl: String?,
    val memberSince: String
)
