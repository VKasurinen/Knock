package com.vkasurinen.knock.auth.presentation.di

import com.vkasurinen.knock.auth.presentation.email_verification.EmailVerificationViewModel
import com.vkasurinen.knock.auth.presentation.forgot_password.ForgotPasswordViewModel
import com.vkasurinen.knock.auth.presentation.login.LoginViewModel
import com.vkasurinen.knock.auth.presentation.register.RegisterViewModel
import com.vkasurinen.knock.auth.presentation.register_success.RegisterSuccessViewModel
import com.vkasurinen.knock.auth.presentation.reset_password.ResetPasswordViewModel
import com.vkasurinen.knock.auth.presentation.verification_pending.VerificationPendingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::VerificationPendingViewModel)
}