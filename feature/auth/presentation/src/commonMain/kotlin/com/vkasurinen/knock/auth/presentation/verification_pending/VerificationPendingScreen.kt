package com.vkasurinen.knock.auth.presentation.verification_pending

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knock.feature.auth.presentation.generated.resources.Res
import knock.feature.auth.presentation.generated.resources.back_to_login
import knock.feature.auth.presentation.generated.resources.resend_verification_email
import knock.feature.auth.presentation.generated.resources.resent_verification_email
import knock.feature.auth.presentation.generated.resources.verification_email_already_sent_desc
import knock.feature.auth.presentation.generated.resources.verification_email_sent
import knock.feature.auth.presentation.generated.resources.verification_email_sent_desc
import knock.feature.auth.presentation.generated.resources.verify_your_email
import com.vkasurinen.knock.core.designsystem.components.brand.KnockSuccessIcon
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockAdaptiveResultLayout
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockSimpleResultLayout
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockSnackbarScaffold
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VerificationPendingRoot(
    viewModel: VerificationPendingViewModel = koinViewModel(),
    onBackToLoginClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            VerificationPendingEvent.ResendVerificationEmailSuccess -> {
                snackbarHostState.showSnackbar(
                    message = getString(Res.string.resent_verification_email)
                )
            }
        }
    }

    VerificationPendingScreen(
        state = state,
        onAction = { action ->
            when(action) {
                VerificationPendingAction.OnBackToLoginClick -> onBackToLoginClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun VerificationPendingScreen(
    state: VerificationPendingState,
    onAction: (VerificationPendingAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    KnockSnackbarScaffold(
        snackbarHostState = snackbarHostState
    ) {
        KnockAdaptiveResultLayout {
            KnockSimpleResultLayout(
                title = if(state.verificationEmailSent) {
                    stringResource(Res.string.verification_email_sent)
                } else {
                    stringResource(Res.string.verify_your_email)
                },
                description = if(state.verificationEmailSent) {
                    stringResource(Res.string.verification_email_sent_desc, state.email)
                } else {
                    stringResource(Res.string.verification_email_already_sent_desc, state.email)
                },
                icon = {
                    KnockSuccessIcon()
                },
                primaryButton = {
                    KnockButton(
                        text = stringResource(Res.string.back_to_login),
                        onClick = {
                            onAction(VerificationPendingAction.OnBackToLoginClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                },
                secondaryButton = {
                    KnockButton(
                        text = stringResource(Res.string.resend_verification_email),
                        onClick = {
                            onAction(VerificationPendingAction.OnResendVerificationEmailClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = !state.isResendingVerificationEmail,
                        isLoading = state.isResendingVerificationEmail,
                        style = KnockButtonStyle.SECONDARY
                    )
                },
                secondaryError = state.resendVerificationError?.asString()
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    KnockTheme {
        VerificationPendingScreen(
            state = VerificationPendingState(
                email = "test@preview.com",
                verificationEmailSent = true
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
