package com.vkasurinen.knock.auth.presentation.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.vkasurinen.knock.core.designsystem.theme.extended
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import knock.feature.auth.presentation.generated.resources.Res
import knock.feature.auth.presentation.generated.resources.create_account
import knock.feature.auth.presentation.generated.resources.email
import knock.feature.auth.presentation.generated.resources.email_placeholder
import knock.feature.auth.presentation.generated.resources.forgot_password
import knock.feature.auth.presentation.generated.resources.login
import knock.feature.auth.presentation.generated.resources.password
import knock.feature.auth.presentation.generated.resources.welcome_back
import com.vkasurinen.knock.core.designsystem.components.brand.KnockBrandLogo
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockAdaptiveFormLayout
import com.vkasurinen.knock.core.designsystem.components.layouts.KnockSnackbarScaffold
import com.vkasurinen.knock.core.designsystem.components.textfields.KnockPasswordTextField
import com.vkasurinen.knock.core.designsystem.components.textfields.KnockTextField
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoot(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onEmailNotVerified: (email: String, verificationEmailSent: Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            LoginEvent.Success -> onLoginSuccess()
            is LoginEvent.EmailNotVerified -> {
                onEmailNotVerified(event.email, event.verificationEmailSent)
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when(action) {
                LoginAction.OnForgotPasswordClick -> onForgotPasswordClick()
                LoginAction.OnSignUpClick -> onCreateAccountClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    KnockSnackbarScaffold {
        KnockAdaptiveFormLayout(
            headerText = stringResource(Res.string.welcome_back),
            errorText = state.error?.asString(),
            logo = {
                KnockBrandLogo()
            },
            modifier = Modifier
                .fillMaxSize()
        ) {
            KnockTextField(
                state = state.emailTextFieldState,
                placeholder = stringResource(Res.string.email_placeholder),
                keyboardType = KeyboardType.Email,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(),
                title = stringResource(Res.string.email)
            )
            Spacer(modifier = Modifier.height(16.dp))
            KnockPasswordTextField(
                state = state.passwordTextFieldState,
                placeholder = stringResource(Res.string.password),
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibilityClick = {
                    onAction(LoginAction.OnTogglePasswordVisibility)
                },
                title = stringResource(Res.string.password),
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.forgot_password),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.extended.tertiary,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        onAction(LoginAction.OnForgotPasswordClick)
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))

            KnockButton(
                text = stringResource(Res.string.login),
                onClick = {
                    onAction(LoginAction.OnLoginClick)
                },
                enabled = state.canLogin,
                isLoading = state.isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            KnockButton(
                text = stringResource(Res.string.create_account),
                onClick = {
                    onAction(LoginAction.OnSignUpClick)
                },
                style = KnockButtonStyle.SECONDARY,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun LightThemePreview() {
    KnockTheme {
        LoginScreen(
            state = LoginState(),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun DarkThemePreview() {
    KnockTheme(darkTheme = true) {
        LoginScreen(
            state = LoginState(),
            onAction = {}
        )
    }
}