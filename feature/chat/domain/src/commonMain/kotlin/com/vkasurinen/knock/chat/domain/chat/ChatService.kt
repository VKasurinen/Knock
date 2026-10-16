package com.vkasurinen.knock.chat.domain.chat

import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result

interface ChatService {
    suspend fun createChat(
        otherUserIds: List<String>,
        name: String? = null
    ): Result<Chat, DataError.Remote>

    suspend fun renameChat(
        chatId: String,
        name: String?
    ): Result<Chat, DataError.Remote>

    suspend fun getChats(): Result<List<Chat>, DataError.Remote>

    suspend fun getChatById(chatId: String): Result<Chat, DataError.Remote>

    /** The chat's messages have reached this device */
    suspend fun markDelivered(chatId: String): EmptyResult<DataError.Remote>
    /** The user has seen the chat's messages */
    suspend fun markRead(chatId: String): EmptyResult<DataError.Remote>
    suspend fun markAllDelivered(): EmptyResult<DataError.Remote>

    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
    suspend fun removeParticipant(chatId: String, userId: String): EmptyResult<DataError.Remote>

    suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<Chat, DataError.Remote>
}