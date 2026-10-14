package com.vkasurinen.knock.chat.data.mappers

import com.vkasurinen.knock.chat.data.dto.AttachmentSerializable
import com.vkasurinen.knock.chat.data.dto.ChatMessageAttachmentDto
import com.vkasurinen.knock.chat.data.dto.ChatMessageDto
import com.vkasurinen.knock.chat.data.dto.websocket.IncomingWebSocketDto
import com.vkasurinen.knock.chat.data.dto.websocket.OutgoingWebSocketDto
import com.vkasurinen.knock.chat.data.dto.websocket.SendAttachmentDto
import com.vkasurinen.knock.chat.database.entities.ChatMessageEntity
import com.vkasurinen.knock.chat.database.view.LastMessageView
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.chat.domain.models.OutgoingNewMessage
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

private val attachmentsJson = Json {
    ignoreUnknownKeys = true
}

fun ChatMessageDto.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = Instant.parse(createdAt),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT,
        attachments = attachments.map { it.toDomain() }
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = messageId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(deliveryStatus),
        attachments = decodeAttachments(attachments).map { it.toDomain() }
    )
}

fun LastMessageView.toDomain(): ChatMessage {
    return ChatMessage(
        id = messageId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(this.deliveryStatus),
        attachments = decodeAttachments(attachments).map { it.toDomain() }
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name,
        attachments = encodeAttachments(attachments.map { it.toSerializable() })
    )
}

fun ChatMessage.toLastMessageView(): LastMessageView {
    return LastMessageView(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name,
        senderUsername = null,
        attachments = encodeAttachments(attachments.map { it.toSerializable() })
    )
}

/**
 * @param localPaths local copies of the attachments, by position. The server doesn't know
 * about them, so they are carried over from the message the local user sent.
 */
fun IncomingWebSocketDto.NewMessageDto.toEntity(
    localPaths: List<String?> = emptyList()
): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = Instant.parse(createdAt).toEpochMilliseconds(),
        deliveryStatus = ChatMessageDeliveryStatus.SENT.name,
        attachments = encodeAttachments(
            attachments.mapIndexed { index, attachment ->
                attachment.toSerializable(localPath = localPaths.getOrNull(index))
            }
        )
    )
}

fun OutgoingNewMessage.toEntity(
    senderId: String,
    deliveryStatus: ChatMessageDeliveryStatus
): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = messageId,
        chatId = chatId,
        content = content,
        senderId = senderId,
        deliveryStatus = deliveryStatus.name,
        timestamp = Clock.System.now().toEpochMilliseconds(),
        attachments = encodeAttachments(attachments.map { it.toSerializable() })
    )
}

/**
 * Only valid once every attachment has been uploaded and has a URL.
 */
fun ChatMessageEntity.toWebSocketDto(): OutgoingWebSocketDto.NewMessage {
    return OutgoingWebSocketDto.NewMessage(
        chatId = chatId,
        messageId = messageId,
        content = content,
        attachments = decodeAttachments(attachments).map { attachment ->
            SendAttachmentDto(
                url = requireNotNull(attachment.url) { "Attachment hasn't been uploaded" },
                fileName = attachment.fileName,
                mimeType = attachment.mimeType,
                sizeBytes = attachment.sizeBytes,
                width = attachment.width,
                height = attachment.height,
                durationMs = attachment.durationMs
            )
        }
    )
}

fun decodeAttachments(json: String): List<AttachmentSerializable> {
    // Most messages have no attachments, so skip parsing for them
    if(json.isBlank() || json == EMPTY_ATTACHMENTS) {
        return emptyList()
    }
    return attachmentsJson.decodeFromString(json)
}

fun encodeAttachments(attachments: List<AttachmentSerializable>): String {
    if(attachments.isEmpty()) {
        return EMPTY_ATTACHMENTS
    }
    return attachmentsJson.encodeToString(attachments)
}

private const val EMPTY_ATTACHMENTS = "[]"

fun ChatMessageAttachmentDto.toDomain(): MessageAttachment {
    return toSerializable(localPath = null).toDomain()
}

fun ChatMessageAttachmentDto.toSerializable(localPath: String?): AttachmentSerializable {
    return AttachmentSerializable(
        id = id,
        type = type,
        url = url,
        localPath = localPath,
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        durationMs = durationMs
    )
}

fun AttachmentSerializable.toDomain(): MessageAttachment {
    return MessageAttachment(
        id = id,
        type = AttachmentType.entries.find { it.name == type } ?: AttachmentType.IMAGE,
        url = url,
        localPath = localPath,
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        durationMs = durationMs
    )
}

fun MessageAttachment.toSerializable(): AttachmentSerializable {
    return AttachmentSerializable(
        id = id,
        type = type.name,
        url = url,
        localPath = localPath,
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        durationMs = durationMs
    )
}

fun OutgoingAttachment.toSerializable(): AttachmentSerializable {
    return AttachmentSerializable(
        type = type.name,
        localPath = localPath,
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        durationMs = durationMs
    )
}
