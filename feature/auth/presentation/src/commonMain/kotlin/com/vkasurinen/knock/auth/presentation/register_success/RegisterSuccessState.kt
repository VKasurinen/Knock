package com.vkasurinen.knock.auth.presentation.register_success

import com.vkasurinen.knock.core.presentation.util.UiText

data class RegisterSuccessState(
    val registeredEmail: String = "",
    val isResendingVerificationEmail: Boolean = false,
    val resendVerificationError: UiText? = null
)