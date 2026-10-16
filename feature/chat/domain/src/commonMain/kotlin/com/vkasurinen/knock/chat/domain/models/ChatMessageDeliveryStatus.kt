package com.vkasurinen.knock.chat.domain.models

enum class ChatMessageDeliveryStatus {
    SENDING,
    SENT,
    FAILED,
    /** Shown for sent messages that reached every other participant. Never stored. */
    DELIVERED,
    /** Shown for sent messages that every other participant has seen. Never stored. */
    READ
}