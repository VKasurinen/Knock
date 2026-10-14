package com.vkasurinen.knock.chat.presentation.chat_list

import com.vkasurinen.knock.core.presentation.util.UiText

sealed interface ChatListEvent {
    data object OnLogoutSuccess: ChatListEvent
    data class OnLogoutError(val error: UiText): ChatListEvent
    data object OnChatDeleted: ChatListEvent
}