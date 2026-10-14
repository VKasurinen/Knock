package com.vkasurinen.knock.chat.data.typing

import com.vkasurinen.knock.chat.data.dto.websocket.OutgoingWebSocketDto
import com.vkasurinen.knock.chat.data.dto.websocket.WebSocketMessageDto
import com.vkasurinen.knock.chat.data.network.KtorWebSocketConnector
import com.vkasurinen.knock.chat.domain.chat.ChatConnectionClient
import com.vkasurinen.knock.chat.domain.typing.TypingRepository
import com.vkasurinen.knock.chat.domain.typing.TypingThrottler
import com.vkasurinen.knock.chat.domain.typing.typingUserIds
import com.vkasurinen.knock.core.domain.preferences.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class WebSocketTypingRepository(
    private val connectionClient: ChatConnectionClient,
    private val webSocketConnector: KtorWebSocketConnector,
    private val userPreferences: UserPreferences,
    private val json: Json
): TypingRepository {

    private val throttler = TypingThrottler()
    private val throttlerMutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTypingUserIds(chatId: String): Flow<List<String>> {
        return userPreferences
            .observeTypingIndicatorsEnabled()
            .flatMapLatest { isEnabled ->
                if(isEnabled) {
                    connectionClient.chatEvents.typingUserIds(chatId)
                } else {
                    flowOf(emptyList())
                }
            }
    }

    override suspend fun onLocalUserTyping(chatId: String) {
        if(!userPreferences.observeTypingIndicatorsEnabled().first()) {
            return
        }
        val shouldSend = throttlerMutex.withLock {
            throttler.shouldSend(chatId)
        }
        if(!shouldSend) {
            return
        }

        val typing = OutgoingWebSocketDto.Typing(chatId)
        val message = WebSocketMessageDto(
            type = typing.type.name,
            payload = json.encodeToString(typing)
        )
        // Best effort, a missed typing event is not worth reporting
        webSocketConnector.sendMessage(json.encodeToString(message))
    }
}
