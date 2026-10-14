package com.vkasurinen.knock.chat.domain.models

/**
 * Real-time events from the chat connection that are not persisted as messages.
 */
sealed interface ChatEvent {
    data class UserTyping(
        val chatId: String,
        val userId: String
    ): ChatEvent

    data class ParticipantRemoved(
        val chatId: String,
        val userId: String,
        val username: String?,
        val isLocalUser: Boolean
    ): ChatEvent

    data class ChatDeleted(
        val chatId: String
    ): ChatEvent
}
