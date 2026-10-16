package com.vkasurinen.knock.chat.domain.chat

import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.domain.models.ChatInfo
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import com.vkasurinen.knock.chat.domain.models.ChatReadState
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getChatInfoById(chatId: String): Flow<ChatInfo>
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipant>>
    suspend fun fetchChats(): Result<List<Chat>, DataError.Remote>
    suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote>
    fun getChatParticipant(userId: String): Flow<ChatParticipant?>
    /** Group chats that both the local user and [otherUserId] are active members of. */
    fun getCommonGroups(localUserId: String, otherUserId: String): Flow<List<Chat>>
    suspend fun createChat(
        otherUserIds: List<String>,
        name: String? = null
    ): Result<Chat, DataError.Remote>
    /** Returns the direct chat with [userId], creating it on the server if there isn't one yet. */
    suspend fun getOrCreateDirectChat(userId: String): Result<Chat, DataError.Remote>
    suspend fun renameChat(chatId: String, name: String?): Result<Chat, DataError.Remote>
    suspend fun markChatDelivered(chatId: String): EmptyResult<DataError.Remote>
    suspend fun markChatRead(chatId: String): EmptyResult<DataError.Remote>
    suspend fun markAllChatsDelivered(): EmptyResult<DataError.Remote>
    suspend fun updateReadState(chatId: String, readState: ChatReadState)
    suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote>
    suspend fun removeParticipant(chatId: String, userId: String): EmptyResult<DataError.Remote>
    suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<Chat, DataError.Remote>
    suspend fun deleteAllChats()
}