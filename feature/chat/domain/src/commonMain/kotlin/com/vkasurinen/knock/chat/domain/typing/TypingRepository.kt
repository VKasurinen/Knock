package com.vkasurinen.knock.chat.domain.typing

import kotlinx.coroutines.flow.Flow

interface TypingRepository {
    /**
     * Ids of the other users currently typing in [chatId], in the order they started typing.
     * Always empty when the user has disabled typing indicators.
     */
    fun observeTypingUserIds(chatId: String): Flow<List<String>>

    /**
     * Lets the other participants know the local user is typing. Throttled, and a no-op
     * when the user has disabled typing indicators.
     */
    suspend fun onLocalUserTyping(chatId: String)
}
