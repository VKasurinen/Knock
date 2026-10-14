package com.vkasurinen.knock.chat.domain.chat

import com.vkasurinen.knock.chat.domain.error.ConnectionError
import com.vkasurinen.knock.chat.domain.models.ChatEvent
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ConnectionState
import com.vkasurinen.knock.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatConnectionClient {
    val chatMessages: Flow<ChatMessage>
    val chatEvents: Flow<ChatEvent>
    val connectionState: StateFlow<ConnectionState>
}