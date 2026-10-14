package com.vkasurinen.knock.chat.presentation.profile

sealed interface ProfileEvent {
    data object OnSaved: ProfileEvent
    data object OnClose: ProfileEvent
}
