package com.vkasurinen.knock.auth.presentation.login

sealed interface LoginEvent {
    data object Success: LoginEvent
    data class EmailNotVerified(
        val email: String,
        val verificationEmailSent: Boolean
    ): LoginEvent
}