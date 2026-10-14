package com.vkasurinen.knock.core.data.auth

import com.vkasurinen.knock.core.data.dto.AuthInfoSerializable
import com.vkasurinen.knock.core.data.dto.requests.ChangePasswordRequest
import com.vkasurinen.knock.core.data.dto.requests.EmailRequest
import com.vkasurinen.knock.core.data.dto.requests.LoginRequest
import com.vkasurinen.knock.core.data.dto.requests.RefreshRequest
import com.vkasurinen.knock.core.data.dto.requests.RegisterRequest
import com.vkasurinen.knock.core.data.dto.requests.ResetPasswordRequest
import com.vkasurinen.knock.core.data.mappers.toDomain
import com.vkasurinen.knock.core.data.dto.ErrorResponseSerializable
import com.vkasurinen.knock.core.data.networking.constructRoute
import com.vkasurinen.knock.core.data.networking.get
import com.vkasurinen.knock.core.data.networking.platformSafeCall
import com.vkasurinen.knock.core.data.networking.post
import com.vkasurinen.knock.core.data.networking.responseToResult
import com.vkasurinen.knock.core.domain.auth.AuthInfo
import com.vkasurinen.knock.core.domain.auth.AuthService
import com.vkasurinen.knock.core.domain.auth.LoginError
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

private const val EMAIL_NOT_VERIFIED_CODE = "EMAIL_NOT_VERIFIED"


class KtorAuthService(
    private val httpClient: HttpClient
): AuthService {

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthInfo, LoginError> {
        var emailNotVerifiedError: LoginError.EmailNotVerified? = null

        val result = platformSafeCall(
            execute = {
                httpClient.post {
                    url(constructRoute("/auth/login"))
                    setBody(
                        LoginRequest(
                            email = email,
                            password = password
                        )
                    )
                }
            },
            handleResponse = { response ->
                if(response.status == HttpStatusCode.Forbidden) {
                    val errorBody = response.errorBodyOrNull()
                    if(errorBody?.code == EMAIL_NOT_VERIFIED_CODE) {
                        emailNotVerifiedError = LoginError.EmailNotVerified(
                            verificationEmailSent = errorBody.verificationEmailSent == true
                        )
                    }
                }
                responseToResult<AuthInfoSerializable>(response)
            }
        )

        return when(result) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Failure -> Result.Failure(
                emailNotVerifiedError ?: LoginError.Remote(result.error)
            )
        }
    }

    private suspend fun HttpResponse.errorBodyOrNull(): ErrorResponseSerializable? {
        return try {
            body<ErrorResponseSerializable>()
        } catch(e: Exception) {
            if(e is CancellationException) throw e
            null
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/register",
            body = RegisterRequest(
                email = email,
                username = username,
                password = password
            )
        )
    }

    override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/resend-verification",
            body = EmailRequest(email),
        )
    }

    override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> {
        return httpClient.get(
            route = "/auth/verify",
            queryParams = mapOf("token" to token)
        )
    }

    override suspend fun forgotPassword(email: String): EmptyResult<DataError.Remote> {
        return httpClient.post<EmailRequest, Unit>(
            route = "/auth/forgot-password",
            body = EmailRequest(email)
        )
    }

    override suspend fun resetPassword(
        newPassword: String,
        token: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/reset-password",
            body = ResetPasswordRequest(
                newPassword = newPassword,
                token = token
            )
        )
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/change-password",
            body = ChangePasswordRequest(
                oldPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }

    override suspend fun logout(refreshToken: String): EmptyResult<DataError.Remote> {
        return httpClient.post<RefreshRequest, Unit>(
            route = "/auth/logout",
            body = RefreshRequest(refreshToken)
        ).onSuccess {
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        }
    }
}