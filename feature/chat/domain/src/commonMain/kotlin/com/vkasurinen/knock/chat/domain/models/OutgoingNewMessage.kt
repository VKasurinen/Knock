package com.vkasurinen.knock.chat.domain.models

data class OutgoingNewMessage(
    val chatId: String,
    val messageId: String,
    val content: String,
    val attachments: List<OutgoingAttachment> = emptyList()
)