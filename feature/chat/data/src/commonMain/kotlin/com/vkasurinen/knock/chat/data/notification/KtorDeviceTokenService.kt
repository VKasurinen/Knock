package com.vkasurinen.knock.chat.data.notification

import com.vkasurinen.knock.chat.data.dto.request.RegisterDeviceTokenRequest
import com.vkasurinen.knock.chat.domain.notification.DeviceTokenService
import com.vkasurinen.knock.core.data.networking.delete
import com.vkasurinen.knock.core.data.networking.post
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorDeviceTokenService(
    private val httpClient: HttpClient
): DeviceTokenService {

    override suspend fun registerToken(
        token: String,
        platform: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/notification/register",
            body = RegisterDeviceTokenRequest(
                token = token,
                platform = platform
            )
        )
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            route = "/notification/$token"
        )
    }
}