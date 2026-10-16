package com.vkasurinen.knock.chat.data.chat

import kotlinx.coroutines.launch
import kotlin.time.Instant
import com.vkasurinen.knock.chat.domain.models.ChatReadState
import com.vkasurinen.knock.chat.data.dto.websocket.IncomingWebSocketDto
import com.vkasurinen.knock.chat.data.dto.websocket.IncomingWebSocketType
import com.vkasurinen.knock.chat.data.dto.websocket.WebSocketMessageDto
import com.vkasurinen.knock.chat.data.mappers.toDomain
import com.vkasurinen.knock.chat.data.mappers.toEntity
import com.vkasurinen.knock.chat.data.mappers.decodeAttachments
import com.vkasurinen.knock.chat.data.network.KtorWebSocketConnector
import com.vkasurinen.knock.chat.database.KnockChatDatabase
import com.vkasurinen.knock.chat.domain.chat.ChatConnectionClient
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.message.MessageRepository
import com.vkasurinen.knock.chat.domain.models.ChatEvent
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.ConnectionState
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json

class WebSocketChatConnectionClient(
    private val webSocketConnector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val database: KnockChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val applicationScope: CoroutineScope
): ChatConnectionClient {

    // The connector's flow is cold and opens a new WebSocket per collector,
    // so everything needs to go through this single shared flow.
    private val incomingMessages = webSocketConnector
        .messages
        .mapNotNull { parseIncomingMessage(it) }
        .map { message ->
            HandledMessage(
                message = message,
                event = handleIncomingMessage(message)
            )
        }
        .shareIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5000)
        )

    override val chatMessages = incomingMessages
        .map { it.message }
        .filterIsInstance<IncomingWebSocketDto.NewMessageDto>()
        .mapNotNull {
            database.chatMessageDao.getMessageById(it.id)?.toDomain()
        }
        .shareIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5000)
        )

    override val chatEvents: Flow<ChatEvent> = incomingMessages.mapNotNull { it.event }

    override val connectionState = webSocketConnector.connectionState

    private fun parseIncomingMessage(message: WebSocketMessageDto): IncomingWebSocketDto? {
        return when(message.type) {
            IncomingWebSocketType.NEW_MESSAGE.name -> {
                json.decodeFromString<IncomingWebSocketDto.NewMessageDto>(message.payload)
            }
            IncomingWebSocketType.MESSAGE_DELETED.name -> {
                json.decodeFromString<IncomingWebSocketDto.MessageDeletedDto>(message.payload)
            }
            IncomingWebSocketType.PROFILE_PICTURE_UPDATED.name -> {
                json.decodeFromString<IncomingWebSocketDto.ProfilePictureUpdated>(message.payload)
            }
            IncomingWebSocketType.CHAT_PARTICIPANTS_CHANGED.name -> {
                json.decodeFromString<IncomingWebSocketDto.ChatParticipantsChangedDto>(message.payload)
            }
            IncomingWebSocketType.CHAT_UPDATED.name -> {
                json.decodeFromString<IncomingWebSocketDto.ChatUpdatedDto>(message.payload)
            }
            IncomingWebSocketType.READ_STATE_CHANGED.name -> {
                json.decodeFromString<IncomingWebSocketDto.ReadStateChangedDto>(message.payload)
            }
            IncomingWebSocketType.USER_TYPING.name -> {
                json.decodeFromString<IncomingWebSocketDto.UserTypingDto>(message.payload)
            }
            IncomingWebSocketType.PARTICIPANT_REMOVED.name -> {
                json.decodeFromString<IncomingWebSocketDto.ParticipantRemovedDto>(message.payload)
            }
            IncomingWebSocketType.CHAT_DELETED.name -> {
                json.decodeFromString<IncomingWebSocketDto.ChatDeletedDto>(message.payload)
            }
            else -> null
        }
    }

    /**
     * Applies the message to the local database and returns the event to emit for it, if any.
     */
    private suspend fun handleIncomingMessage(message: IncomingWebSocketDto): ChatEvent? {
        return when(message) {
            is IncomingWebSocketDto.ChatParticipantsChangedDto -> {
                chatRepository.fetchChatById(message.chatId)
                null
            }
            is IncomingWebSocketDto.ChatUpdatedDto -> {
                chatRepository.fetchChatById(message.chatId)
                null
            }
            is IncomingWebSocketDto.ReadStateChangedDto -> {
                chatRepository.updateReadState(
                    chatId = message.chatId,
                    readState = ChatReadState(
                        userId = message.userId,
                        lastDeliveredAt = message.lastDeliveredAt?.let(Instant::parse),
                        lastReadAt = message.lastReadAt?.let(Instant::parse)
                    )
                )
                null
            }
            is IncomingWebSocketDto.MessageDeletedDto -> {
                deleteMessage(message)
                null
            }
            is IncomingWebSocketDto.NewMessageDto -> {
                handleNewMessage(message)
                null
            }
            is IncomingWebSocketDto.ProfilePictureUpdated -> {
                updateProfilePicture(message)
                null
            }
            is IncomingWebSocketDto.UserTypingDto -> ChatEvent.UserTyping(
                chatId = message.chatId,
                userId = message.userId
            )
            is IncomingWebSocketDto.ParticipantRemovedDto -> removeParticipant(message)
            is IncomingWebSocketDto.ChatDeletedDto -> deleteChat(message)
        }
    }

    private suspend fun removeParticipant(
        message: IncomingWebSocketDto.ParticipantRemovedDto
    ): ChatEvent {
        val localUserId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id
        val isLocalUser = message.userId == localUserId

        if(isLocalUser) {
            // Keep the chat and its history visible, but read-only
            database.chatDao.markLocalUserRemoved(message.chatId)
        } else {
            database.chatParticipantsCrossRefDao.markParticipantsAsInactive(
                chatId = message.chatId,
                userIds = listOf(message.userId)
            )
        }

        return ChatEvent.ParticipantRemoved(
            chatId = message.chatId,
            userId = message.userId,
            username = database.chatParticipantDao.getParticipantById(message.userId)?.username,
            isLocalUser = isLocalUser
        )
    }

    private suspend fun deleteChat(message: IncomingWebSocketDto.ChatDeletedDto): ChatEvent? {
        // The admin who deleted the chat already removed it locally when leaving
        database.chatDao.getChatById(message.chatId) ?: return null

        database.chatDao.deleteChatById(message.chatId)
        return ChatEvent.ChatDeleted(chatId = message.chatId)
    }

    private suspend fun deleteMessage(message: IncomingWebSocketDto.MessageDeletedDto) {
        database.chatMessageDao.deleteMessageById(message.messageId)
    }

    private suspend fun handleNewMessage(message: IncomingWebSocketDto.NewMessageDto) {
        val chat = database.chatDao.getChatById(message.chatId)?.chat
        if(chat?.isLocalUserRemoved == true) {
            return
        }
        if(chat == null) {
            chatRepository.fetchChatById(message.chatId)
        }

        // Keep the local copies of attachments the local user sent, so they don't need downloading
        val localPaths = database.chatMessageDao.getMessageById(message.id)
            ?.let { existing -> decodeAttachments(existing.attachments).map { it.localPath } }
            .orEmpty()
        database.chatMessageDao.upsertMessage(message.toEntity(localPaths))

        // Tell the sender the message reached this device
        val localUserId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id
        if(message.senderId != localUserId) {
            applicationScope.launch {
                chatRepository.markChatDelivered(message.chatId)
            }
        }
    }

    private suspend fun updateProfilePicture(message: IncomingWebSocketDto.ProfilePictureUpdated) {
        database.chatParticipantDao.updateProfilePictureUrl(
            userId = message.userId,
            newUrl = message.newUrl
        )

        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
        if(authInfo != null && authInfo.user.id == message.userId) {
            sessionStorage.set(
                info = authInfo.copy(
                    user = authInfo.user.copy(
                        profilePictureUrl = message.newUrl
                    )
                )
            )
        }
    }

    private class HandledMessage(
        val message: IncomingWebSocketDto,
        val event: ChatEvent?
    )
}
