package com.vkasurinen.knock.chat.data.mappers

import com.vkasurinen.knock.chat.data.dto.ChatDto
import com.vkasurinen.knock.chat.database.entities.ChatEntity
import com.vkasurinen.knock.chat.database.entities.ChatInfoEntity
import com.vkasurinen.knock.chat.database.entities.ChatWithParticipants
import com.vkasurinen.knock.chat.database.entities.MessageWithSender
import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.domain.models.ChatInfo
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import kotlin.time.Instant


typealias DataMessageWithSender = MessageWithSender
typealias DomainMessageWithSender = com.vkasurinen.knock.chat.domain.models.MessageWithSender

fun ChatDto.toDomain(): Chat {
    val lastMessageSenderUsername = lastMessage?.let { message ->
        participants.find { it.userId == message.senderId }?.username
    }
    return Chat(
        id = id,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.parse(lastActivityAt),
        lastMessage = lastMessage?.toDomain(),
        lastMessageSenderUsername = lastMessageSenderUsername,
        creatorId = creator?.userId
    )
}

fun ChatEntity.toDomain(
    participants: List<ChatParticipant>,
    lastMessage: ChatMessage? = null
): Chat {
    val lastMessageSenderUsername = lastMessage?.let { message ->
        participants.find { it.userId == message.senderId }?.username
    }
    return Chat(
        id = chatId,
        participants = participants,
        lastActivityAt = Instant.fromEpochMilliseconds(lastActivityAt),
        lastMessage = lastMessage,
        lastMessageSenderUsername = lastMessageSenderUsername,
        creatorId = creatorId,
        isLocalUserRemoved = isLocalUserRemoved
    )
}

fun ChatWithParticipants.toDomain(): Chat {
    return Chat(
        id = chat.chatId,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.fromEpochMilliseconds(chat.lastActivityAt),
        lastMessage = lastMessage?.toDomain(),
        lastMessageSenderUsername = lastMessage?.senderUsername,
        creatorId = chat.creatorId,
        isLocalUserRemoved = chat.isLocalUserRemoved
    )
}

fun Chat.toEntity(): ChatEntity {
    return ChatEntity(
        chatId = id,
        lastActivityAt = lastActivityAt.toEpochMilliseconds(),
        creatorId = creatorId,
        isLocalUserRemoved = isLocalUserRemoved
    )
}

fun DataMessageWithSender.toDomain(): DomainMessageWithSender {
    return DomainMessageWithSender(
        message = message.toDomain(),
        sender = sender.toDomain(),
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(this.message.deliveryStatus)
    )
}

fun ChatInfoEntity.toDomain(): ChatInfo {
    return ChatInfo(
        chat = chat.toDomain(
            participants = this.participants.map { it.toDomain() }
        ),
        messages = messagesWithSenders.map { it.toDomain() }
    )
}