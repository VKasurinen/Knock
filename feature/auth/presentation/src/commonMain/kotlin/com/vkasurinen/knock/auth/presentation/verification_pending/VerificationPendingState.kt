package com.vkasurinen.knock.auth.presentation.verification_pending

import com.vkasurinen.knock.core.presentation.util.UiText

data class VerificationPendingState(
    val email: String = "",
    val verificationEmailSent: Boolean = false,
    val isResendingVerificationEmail: Boolean = false,
    val resendVerificationError: UiText? = null
)
