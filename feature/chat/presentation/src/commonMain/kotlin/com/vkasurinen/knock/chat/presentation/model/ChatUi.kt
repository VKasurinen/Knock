package com.vkasurinen.knock.chat.presentation.model

import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi

data class ChatUi(
    val id: String,
    val localParticipant: ChatParticipantUi,
    val otherParticipants: List<ChatParticipantUi>,
    val lastMessage: ChatMessage?,
    val lastMessageSenderUsername: String?,
    val creatorId: String? = null,
    val isLocalUserAdmin: Boolean = false,
    val isLocalUserRemoved: Boolean = false
)