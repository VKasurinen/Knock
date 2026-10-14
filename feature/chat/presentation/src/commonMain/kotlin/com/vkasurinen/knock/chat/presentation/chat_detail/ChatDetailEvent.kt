package com.vkasurinen.knock.chat.presentation.chat_detail

import com.vkasurinen.knock.core.presentation.util.UiText

sealed interface ChatDetailEvent {
    data object OnChatLeft: ChatDetailEvent
    data class OnError(val error: UiText): ChatDetailEvent
    data class OnInfo(val message: UiText): ChatDetailEvent
    data object OnNewMessage: ChatDetailEvent
}