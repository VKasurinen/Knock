package com.vkasurinen.knock.chat.presentation.mappers

import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.presentation.model.ChatUi


fun Chat.toUi(localParticipantId: String): ChatUi {
    val (local, other) = participants.partition { it.userId == localParticipantId }
    return ChatUi(
        id = id,
        localParticipant = local.first().toUi(),
        otherParticipants = other.map { it.toUi() },
        lastMessage = lastMessage,
        lastMessageSenderUsername = lastMessageSenderUsername,
        creatorId = creatorId,
        isLocalUserAdmin = creatorId == localParticipantId,
        isLocalUserRemoved = isLocalUserRemoved
    )
}