package com.vkasurinen.knock.chat.presentation.user_profile

sealed interface UserProfileEvent {
    data class OpenChat(val chatId: String): UserProfileEvent
    data class CreateGroupWith(val userId: String): UserProfileEvent
}
