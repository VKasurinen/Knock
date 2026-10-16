package com.vkasurinen.knock.chat.data.chat

import com.vkasurinen.knock.chat.data.dto.ChatDto
import com.vkasurinen.knock.chat.data.dto.request.CreateChatRequest
import com.vkasurinen.knock.chat.data.dto.request.ParticipantsRequest
import com.vkasurinen.knock.chat.data.dto.request.RenameChatRequest
import com.vkasurinen.knock.chat.data.mappers.toDomain
import com.vkasurinen.knock.chat.domain.chat.ChatService
import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.core.data.networking.delete
import com.vkasurinen.knock.core.data.networking.get
import com.vkasurinen.knock.core.data.networking.patch
import com.vkasurinen.knock.core.data.networking.post
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.asEmptyResult
import com.vkasurinen.knock.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.post

class KtorChatService(
    private val httpClient: HttpClient
): ChatService {

    override suspend fun createChat(
        otherUserIds: List<String>,
        name: String?
    ): Result<Chat, DataError.Remote> {
        return httpClient.post<CreateChatRequest, ChatDto>(
            route = "/chat",
            body = CreateChatRequest(
                otherUserIds = otherUserIds,
                name = name
            )
        ).map { it.toDomain() }
    }

    override suspend fun renameChat(
        chatId: String,
        name: String?
    ): Result<Chat, DataError.Remote> {
        return httpClient.patch<RenameChatRequest, ChatDto>(
            route = "/chat/$chatId",
            body = RenameChatRequest(name = name)
        ).map { it.toDomain() }
    }

    override suspend fun getChats(): Result<List<Chat>, DataError.Remote> {
        return httpClient.get<List<ChatDto>>(
            route = "/chat"
        ).map { chatDtos ->
            chatDtos.map { it.toDomain() }
        }
    }

    override suspend fun getChatById(chatId: String): Result<Chat, DataError.Remote> {
        return httpClient.get<ChatDto>(
            route = "/chat/$chatId"
        ).map { it.toDomain() }
    }

    override suspend fun markDelivered(chatId: String): EmptyResult<DataError.Remote> {
        return httpClient.post<Unit, Unit>(
            route = "/chat/$chatId/delivered",
            body = Unit
        )
    }

    override suspend fun markRead(chatId: String): EmptyResult<DataError.Remote> {
        return httpClient.post<Unit, Unit>(
            route = "/chat/$chatId/read",
            body = Unit
        )
    }

    override suspend fun markAllDelivered(): EmptyResult<DataError.Remote> {
        return httpClient.post<Unit, Unit>(
            route = "/chat/delivered",
            body = Unit
        )
    }

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/chat/$chatId/leave"
        ).asEmptyResult()
    }

    override suspend fun removeParticipant(
        chatId: String,
        userId: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.delete<Unit>(
            route = "/chat/$chatId/participants/$userId"
        )
    }

    override suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<Chat, DataError.Remote> {
        return httpClient.post<ParticipantsRequest, ChatDto>(
            route = "/chat/$chatId/add",
            body = ParticipantsRequest(
                userIds = userIds
            )
        ).map { it.toDomain() }
    }
}