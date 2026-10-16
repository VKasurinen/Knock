package com.vkasurinen.knock.chat.presentation.chat_list

import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.presentation.util.UiText

data class ChatListState(
    val chats: List<ChatUi> = emptyList(),
    val error: UiText? = null,
    val localParticipant: ChatParticipantUi? = null,
    val isUserMenuOpen: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val selectedChatId: String? = null,
    val isLoading: Boolean = false,
    val filter: ChatFilter = ChatFilter.ALL,
) {
    val visibleChats: List<ChatUi>
        get() = chats.filter { filter.matches(it) }
}