package com.vkasurinen.knock.auth.presentation.email_verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import knock.feature.auth.presentation.generated.resources.Res
import knock.feature.auth.presentation.generated.resources.close
import knock.feature.auth.presentation.generated.resources.email_verified_failed
import knock.feature.auth.presentation.generated.resources.email_verified_failed_desc
import knock.feature.auth.presentation.generated.resources.email_verified_successfully
import knock.feature.auth.presentation.generated.resources.email_verified_successfully_desc
import knock.feature.auth.presentation.generated.resources.login
import knock.feature.auth.presentation.generated.resources.verifying_account
import com.vkasurinen.knock.core.designsystem.components.brand.KnockFailureIcon
import com.vkasurinen.knock.core.designsystem.components.brand.KnockSuccessIcon
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockAdaptiveResultLayout
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockSimpleResultLayout
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockSnackbarScaffold
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EmailVerificationRoot(
    viewModel: EmailVerificationViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmailVerificationScreen(
        state = state,
        onAction = { action ->
            when(action) {
                EmailVerificationAction.OnCloseClick -> onCloseClick()
                EmailVerificationAction.OnLoginClick -> onLoginClick()
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun EmailVerificationScreen(
    state: EmailVerificationState,
    onAction: (EmailVerificationAction) -> Unit,
) {
    KnockSnackbarScaffold {
        KnockAdaptiveResultLayout {
            when {
                state.isVerifying -> {
                    VerifyingContent(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                state.isVerified -> {
                    KnockSimpleResultLayout(
                        title = stringResource(Res.string.email_verified_successfully),
                        description = stringResource(Res.string.email_verified_successfully_desc),
                        icon = {
                            KnockSuccessIcon()
                        },
                        primaryButton = {
                            KnockButton(
                                text = stringResource(Res.string.login),
                                onClick = {
                                    onAction(EmailVerificationAction.OnLoginClick)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
                else -> {
                    KnockSimpleResultLayout(
                        title = stringResource(Res.string.email_verified_failed),
                        description = stringResource(Res.string.email_verified_failed_desc),
                        icon = {
                            Spacer(modifier = Modifier.height(32.dp))
                            KnockFailureIcon()
                            Spacer(modifier = Modifier.height(32.dp))
                        },
                        primaryButton = {
                            KnockButton(
                                text = stringResource(Res.string.close),
                                onClick = {
                                    onAction(EmailVerificationAction.OnCloseClick)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                style = KnockButtonStyle.SECONDARY
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifyingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(Res.string.verifying_account),
            color = MaterialTheme.colorScheme.extended.textSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
private fun EmailVerificationErrorPreview() {
    KnockTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun EmailVerificationVerifyingPreview() {
    KnockTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(
                isVerifying = true
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun EmailVerificationSuccessPreview() {
    KnockTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(
                isVerified = true
            ),
            onAction = {}
        )
    }
}