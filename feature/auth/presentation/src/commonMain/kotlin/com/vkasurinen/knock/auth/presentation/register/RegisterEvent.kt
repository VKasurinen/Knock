package com.vkasurinen.knock.auth.presentation.register

sealed interface RegisterEvent {
    data class Success(val email: String): RegisterEvent
}