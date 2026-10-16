package com.vkasurinen.knock.chat.data.dto.websocket

import com.vkasurinen.knock.chat.data.dto.ChatMessageAttachmentDto
import kotlinx.serialization.Serializable
enum class IncomingWebSocketType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    CHAT_UPDATED,
    READ_STATE_CHANGED,
    USER_TYPING,
    PARTICIPANT_REMOVED,
    CHAT_DELETED
}

@Serializable
sealed interface IncomingWebSocketDto {

    @Serializable
    data class NewMessageDto(
        val id: String,
        val chatId: String,
        val content: String,
        val senderId: String,
        val createdAt: String,
        val attachments: List<ChatMessageAttachmentDto> = emptyList(),
        val type: IncomingWebSocketType = IncomingWebSocketType.NEW_MESSAGE
    ): IncomingWebSocketDto

    @Serializable
    data class MessageDeletedDto(
        val messageId: String,
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.MESSAGE_DELETED
    ): IncomingWebSocketDto

    @Serializable
    data class ProfilePictureUpdated(
        val userId: String,
        val newUrl: String?,
        val type: IncomingWebSocketType = IncomingWebSocketType.PROFILE_PICTURE_UPDATED
    ): IncomingWebSocketDto

    @Serializable
    data class ChatParticipantsChangedDto(
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.CHAT_PARTICIPANTS_CHANGED
    ): IncomingWebSocketDto

    @Serializable
    data class ChatUpdatedDto(
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.CHAT_UPDATED
    ): IncomingWebSocketDto

    @Serializable
    data class ReadStateChangedDto(
        val chatId: String,
        val userId: String,
        val lastDeliveredAt: String? = null,
        val lastReadAt: String? = null,
        val type: IncomingWebSocketType = IncomingWebSocketType.READ_STATE_CHANGED
    ): IncomingWebSocketDto

    @Serializable
    data class UserTypingDto(
        val chatId: String,
        val userId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.USER_TYPING
    ): IncomingWebSocketDto

    @Serializable
    data class ParticipantRemovedDto(
        val chatId: String,
        val userId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.PARTICIPANT_REMOVED
    ): IncomingWebSocketDto

    @Serializable
    data class ChatDeletedDto(
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.CHAT_DELETED
    ): IncomingWebSocketDto
}