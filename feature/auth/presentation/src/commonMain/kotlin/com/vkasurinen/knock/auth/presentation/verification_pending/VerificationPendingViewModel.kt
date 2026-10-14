package com.vkasurinen.knock.auth.presentation.verification_pending

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import knock.feature.auth.presentation.generated.resources.Res
import knock.feature.auth.presentation.generated.resources.error_resend_verification_rate_limited
import com.vkasurinen.knock.core.domain.auth.AuthService
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import com.vkasurinen.knock.core.presentation.util.UiText
import com.vkasurinen.knock.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerificationPendingViewModel(
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventChannel = Channel<VerificationPendingEvent>()
    val events = eventChannel.receiveAsFlow()

    private val email = savedStateHandle.get<String>("email")
        ?: throw IllegalStateException("No email passed to verification pending screen")

    private val _state = MutableStateFlow(VerificationPendingState(
        email = email,
        verificationEmailSent = savedStateHandle.get<Boolean>("verificationEmailSent") ?: false
    ))
    val state = _state.asStateFlow()

    fun onAction(action: VerificationPendingAction) {
        when (action) {
            VerificationPendingAction.OnResendVerificationEmailClick -> resendVerification()
            else -> Unit
        }
    }

    private fun resendVerification() {
        if(state.value.isResendingVerificationEmail) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(
                isResendingVerificationEmail = true,
                resendVerificationError = null
            ) }

            authService
                .resendVerificationEmail(email)
                .onSuccess {
                    _state.update { it.copy(
                        isResendingVerificationEmail = false,
                        verificationEmailSent = true
                    ) }
                    eventChannel.send(VerificationPendingEvent.ResendVerificationEmailSuccess)
                }
                .onFailure { error ->
                    val errorMessage = when(error) {
                        DataError.Remote.TOO_MANY_REQUESTS -> {
                            UiText.Resource(Res.string.error_resend_verification_rate_limited)
                        }
                        else -> error.toUiText()
                    }
                    _state.update { it.copy(
                        isResendingVerificationEmail = false,
                        resendVerificationError = errorMessage
                    ) }
                }
        }
    }
}
