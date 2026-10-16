package com.vkasurinen.knock.chat.presentation.chat_list_detail

data class ChatListDetailState(
    val selectedChatId: String? = null,
    val dialogState: DialogState = DialogState.Hidden
)

sealed interface DialogState {
    data object Hidden: DialogState
    /** [preselectedUserIds] are already selected, e.g. when creating a group from a profile */
    data class CreateChat(val preselectedUserIds: List<String> = emptyList()): DialogState
    data object Profile: DialogState
    data class ManageChat(val chatId: String): DialogState
    data class UserProfile(val userId: String): DialogState
}