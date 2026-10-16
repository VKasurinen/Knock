package com.vkasurinen.knock.chat.data.mappers

import com.vkasurinen.knock.chat.data.dto.ChatDto
import com.vkasurinen.knock.chat.data.dto.ChatReadStateDto
import com.vkasurinen.knock.chat.database.entities.ChatReadStateEntity
import com.vkasurinen.knock.chat.domain.models.ChatReadState
import com.vkasurinen.knock.chat.database.entities.ChatEntity
import com.vkasurinen.knock.chat.database.entities.ChatInfoEntity
import com.vkasurinen.knock.chat.database.entities.ChatWithParticipants
import com.vkasurinen.knock.chat.database.entities.MessageWithSender
import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.domain.models.ChatInfo
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import com.vkasurinen.knock.chat.domain.models.ChatType
import kotlin.time.Instant


typealias DataMessageWithSender = MessageWithSender
typealias DomainMessageWithSender = com.vkasurinen.knock.chat.domain.models.MessageWithSender

fun ChatDto.toDomain(): Chat {
    val lastMessageSenderUsername = lastMessage?.let { message ->
        participants.find { it.userId == message.senderId }?.username
    }
    return Chat(
        id = id,
        type = type.toChatType(),
        name = name,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.parse(lastActivityAt),
        lastMessage = lastMessage?.toDomain(),
        lastMessageSenderUsername = lastMessageSenderUsername,
        creatorId = creator?.userId,
        readStates = readStates.map { it.toDomain() }
    )
}

fun ChatReadStateDto.toDomain(): ChatReadState {
    return ChatReadState(
        userId = userId,
        lastDeliveredAt = lastDeliveredAt?.let(Instant::parse),
        lastReadAt = lastReadAt?.let(Instant::parse)
    )
}

fun ChatReadStateEntity.toDomain(): ChatReadState {
    return ChatReadState(
        userId = userId,
        lastDeliveredAt = lastDeliveredAt?.let(Instant::fromEpochMilliseconds),
        lastReadAt = lastReadAt?.let(Instant::fromEpochMilliseconds)
    )
}

fun ChatReadState.toEntity(chatId: String): ChatReadStateEntity {
    return ChatReadStateEntity(
        chatId = chatId,
        userId = userId,
        lastDeliveredAt = lastDeliveredAt?.toEpochMilliseconds(),
        lastReadAt = lastReadAt?.toEpochMilliseconds()
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
        type = type.toChatType(),
        name = name,
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
        type = chat.type.toChatType(),
        name = chat.name,
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
        type = type.name,
        name = name,
        lastActivityAt = lastActivityAt.toEpochMilliseconds(),
        creatorId = creatorId,
        isLocalUserRemoved = isLocalUserRemoved
    )
}

/** Unknown values fall back to [ChatType.GROUP], which allows the most actions. */
fun String.toChatType(): ChatType {
    return ChatType.entries.firstOrNull { it.name == this } ?: ChatType.GROUP
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
        ).copy(readStates = readStates.map { it.toDomain() }),
        messages = messagesWithSenders.map { it.toDomain() }
    )
}