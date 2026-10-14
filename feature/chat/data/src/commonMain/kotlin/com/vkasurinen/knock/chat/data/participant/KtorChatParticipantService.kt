package com.vkasurinen.knock.chat.data.participant

import com.vkasurinen.knock.chat.data.dto.ChatParticipantDto
import com.vkasurinen.knock.chat.data.dto.request.ConfirmProfilePictureRequest
import com.vkasurinen.knock.chat.data.dto.response.ProfilePictureUploadUrlsResponse
import com.vkasurinen.knock.chat.data.mappers.toDomain
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import com.vkasurinen.knock.chat.domain.models.ProfilePictureUploadUrls
import com.vkasurinen.knock.core.data.networking.constructRoute
import com.vkasurinen.knock.core.data.networking.delete
import com.vkasurinen.knock.core.data.networking.get
import com.vkasurinen.knock.core.data.networking.post
import com.vkasurinen.knock.core.data.networking.put
import com.vkasurinen.knock.core.data.networking.safeCall
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import kotlin.collections.component1
import kotlin.collections.component2

class KtorChatParticipantService(
    private val httpClient: HttpClient
): ChatParticipantService {

    override suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote> {
        return httpClient.get<ChatParticipantDto>(
            route = "/participants",
            queryParams = mapOf(
                "query" to query
            )
        ).map { it.toDomain() }
    }

    override suspend fun getLocalParticipant(): Result<ChatParticipant, DataError.Remote> {
        return httpClient.get<ChatParticipantDto>(
            route = "/participants"
        ).map { it.toDomain() }
    }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrls, DataError.Remote> {
        return httpClient.post<Unit, ProfilePictureUploadUrlsResponse>(
            route = "/participants/profile-picture-upload",
            queryParams = mapOf(
                "mimeType" to mimeType
            ),
            body = Unit
        ).map { it.toDomain() }
    }

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.put {
                url(uploadUrl)
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(imageBytes)
            }
        }
    }

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> {
        return httpClient.post<ConfirmProfilePictureRequest, Unit>(
            route = "/participants/confirm-profile-picture",
            body = ConfirmProfilePictureRequest(publicUrl)
        )
    }

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            route = "/participants/profile-picture"
        )
    }
}