package com.vkasurinen.knock.chat.presentation.create_chat

import com.vkasurinen.knock.chat.domain.models.Chat

sealed interface CreateChatEvent {
    data class OnChatCreated(val chat: Chat): CreateChatEvent
}