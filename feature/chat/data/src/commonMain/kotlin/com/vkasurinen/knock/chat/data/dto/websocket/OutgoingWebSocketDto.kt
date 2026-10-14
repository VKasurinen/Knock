package com.vkasurinen.knock.chat.data.dto.websocket

import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE,
    TYPING
}

@Serializable
sealed class OutgoingWebSocketDto(
    val type: OutgoingWebSocketType
) {

    @Serializable
    data class NewMessage(
        val chatId: String,
        val messageId: String,
        val content: String,
        val attachments: List<SendAttachmentDto> = emptyList()
    ): OutgoingWebSocketDto(OutgoingWebSocketType.NEW_MESSAGE)

    @Serializable
    data class Typing(
        val chatId: String
    ): OutgoingWebSocketDto(OutgoingWebSocketType.TYPING)
}