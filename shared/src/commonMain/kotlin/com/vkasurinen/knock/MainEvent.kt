package com.vkasurinen.knock

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}