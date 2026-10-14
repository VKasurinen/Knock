package com.vkasurinen.knock.auth.presentation.verification_pending

sealed interface VerificationPendingAction {
    data object OnBackToLoginClick: VerificationPendingAction
    data object OnResendVerificationEmailClick: VerificationPendingAction
}
