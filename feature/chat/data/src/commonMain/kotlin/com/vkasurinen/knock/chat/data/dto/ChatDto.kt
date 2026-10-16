package com.vkasurinen.knock.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String,
    val type: String,
    val name: String? = null,
    val participants: List<ChatParticipantDto>,
    val lastActivityAt: String,
    val lastMessage: ChatMessageDto?,
    val creator: ChatParticipantDto? = null,
    val readStates: List<ChatReadStateDto> = emptyList()
)
