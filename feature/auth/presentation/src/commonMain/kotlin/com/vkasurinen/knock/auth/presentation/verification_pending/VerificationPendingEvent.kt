package com.vkasurinen.knock.auth.presentation.verification_pending

sealed interface VerificationPendingEvent {
    data object ResendVerificationEmailSuccess: VerificationPendingEvent
}
