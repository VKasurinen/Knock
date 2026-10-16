package com.vkasurinen.knock.chat.presentation.user_profile

sealed interface UserProfileAction {
    data class OnSelectUser(val userId: String?): UserProfileAction
    data object OnSendMessageClick: UserProfileAction
    data object OnCreateGroupClick: UserProfileAction
    data class OnGroupClick(val chatId: String): UserProfileAction
    data object OnDismiss: UserProfileAction
}
