package com.vkasurinen.knock.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.vkasurinen.knock.auth.presentation.email_verification.EmailVerificationRoot
import com.vkasurinen.knock.auth.presentation.forgot_password.ForgotPasswordRoot
import com.vkasurinen.knock.auth.presentation.login.LoginRoot
import com.vkasurinen.knock.auth.presentation.register.RegisterRoot
import com.vkasurinen.knock.auth.presentation.register_success.RegisterSuccessRoot
import com.vkasurinen.knock.auth.presentation.reset_password.ResetPasswordRoot
import com.vkasurinen.knock.auth.presentation.verification_pending.VerificationPendingRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit,
) {
    navigation<AuthGraphRoutes.Graph>(
        startDestination = AuthGraphRoutes.Login
    ) {
        composable<AuthGraphRoutes.Login> {
            LoginRoot(
                onLoginSuccess = onLoginSuccess,
                onEmailNotVerified = { email, verificationEmailSent ->
                    navController.navigate(
                        AuthGraphRoutes.VerificationPending(
                            email = email,
                            verificationEmailSent = verificationEmailSent
                        )
                    )
                },
                onForgotPasswordClick = {
                    navController.navigate(AuthGraphRoutes.ForgotPassword)
                },
                onCreateAccountClick = {
                    navController.navigate(AuthGraphRoutes.Register) {
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.Register> {
            RegisterRoot(
                onRegisterSuccess = {
                    navController.navigate(AuthGraphRoutes.RegisterSuccess(it))
                },
                onLoginClick = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo(AuthGraphRoutes.Register) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.RegisterSuccess> {
            RegisterSuccessRoot(
                onLoginClick = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.RegisterSuccess> {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<AuthGraphRoutes.VerificationPending> {
            VerificationPendingRoot(
                onBackToLoginClick = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.VerificationPending> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.EmailVerification>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "http://localhost:8080/api/auth/verify?token={token}"
                },
                navDeepLink {
                    this.uriPattern = "https://knock.vkasurinen.com/api/auth/verify?token={token}"
                },
                navDeepLink {
                    this.uriPattern = "knock://knock.vkasurinen.com/api/auth/verify?token={token}"
                },
            )
        ) {
            EmailVerificationRoot(
                onLoginClick = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.EmailVerification> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCloseClick = {
                    navController.navigate(AuthGraphRoutes.Login) {
                        popUpTo<AuthGraphRoutes.EmailVerification> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<AuthGraphRoutes.ForgotPassword> {
            ForgotPasswordRoot()
        }
        composable<AuthGraphRoutes.ResetPassword>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "http://localhost:8080/api/auth/reset-password?token={token}"
                },
                navDeepLink {
                    this.uriPattern = "https://knock.vkasurinen.com/api/auth/reset-password?token={token}"
                },
                navDeepLink {
                    this.uriPattern = "knock://knock.vkasurinen.com/api/auth/reset-password?token={token}"
                },
            )
        ) {
            ResetPasswordRoot()
        }
    }
}