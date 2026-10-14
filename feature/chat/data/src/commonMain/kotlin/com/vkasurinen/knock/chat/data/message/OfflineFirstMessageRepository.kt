package com.vkasurinen.knock.chat.data.message

import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.data.dto.websocket.OutgoingWebSocketDto
import com.vkasurinen.knock.chat.data.dto.websocket.WebSocketMessageDto
import com.vkasurinen.knock.chat.data.mappers.decodeAttachments
import com.vkasurinen.knock.chat.data.mappers.encodeAttachments
import com.vkasurinen.knock.chat.data.mappers.toDomain
import com.vkasurinen.knock.chat.data.mappers.toEntity
import com.vkasurinen.knock.chat.data.mappers.toWebSocketDto
import com.vkasurinen.knock.chat.data.network.KtorWebSocketConnector
import com.vkasurinen.knock.chat.database.KnockChatDatabase
import com.vkasurinen.knock.chat.database.entities.ChatMessageEntity
import com.vkasurinen.knock.chat.domain.attachment.ChatAttachmentService
import com.vkasurinen.knock.chat.domain.message.ChatMessageService
import com.vkasurinen.knock.chat.domain.message.MessageRepository
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.MessageWithSender
import com.vkasurinen.knock.chat.domain.models.OutgoingNewMessage
import com.vkasurinen.knock.core.data.database.safeDatabaseUpdate
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class OfflineFirstMessageRepository(
    private val database: KnockChatDatabase,
    private val chatMessageService: ChatMessageService,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val webSocketConnector: KtorWebSocketConnector,
    private val chatAttachmentService: ChatAttachmentService,
    private val attachmentFileStorage: AttachmentFileStorage,
    private val applicationScope: CoroutineScope
): MessageRepository {

    private val uploadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())

    override suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError> {
        val localUser = sessionStorage.observeAuthInfo().first()?.user
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        val entity = message.toEntity(
            senderId = localUser.id,
            deliveryStatus = ChatMessageDeliveryStatus.SENDING
        )
        val saveResult = safeDatabaseUpdate {
            database.chatMessageDao.upsertMessage(entity)
        }
        if(saveResult is Result.Failure) {
            return saveResult
        }

        return uploadAndSend(entity)
    }

    override suspend fun retryMessage(messageId: String): EmptyResult<DataError> {
        val message = database.chatMessageDao.getMessageById(messageId)
            ?: return Result.Failure(DataError.Local.NOT_FOUND)

        database.chatMessageDao.updateDeliveryStatus(
            messageId = messageId,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            status = ChatMessageDeliveryStatus.SENDING.name
        )

        return uploadAndSend(message)
    }

    override fun observeUploadProgress(): Flow<Map<String, Float>> {
        return uploadProgress.asStateFlow()
    }

    /**
     * Runs in the application scope, so leaving the chat doesn't cancel an upload halfway.
     */
    private suspend fun uploadAndSend(entity: ChatMessageEntity): EmptyResult<DataError> {
        return applicationScope.async {
            val uploadedEntity = when(val result = uploadAttachments(entity)) {
                is Result.Failure -> {
                    markAsFailed(entity.messageId)
                    return@async result
                }
                is Result.Success -> result.data
            }

            webSocketConnector
                .sendMessage(uploadedEntity.toWebSocketDto().toJsonPayload())
                .onFailure {
                    markAsFailed(entity.messageId)
                }
        }.await()
    }

    /**
     * Uploads every attachment that doesn't have a URL yet. Each finished upload is saved right
     * away, so a retry only uploads what is still missing.
     */
    private suspend fun uploadAttachments(
        entity: ChatMessageEntity
    ): Result<ChatMessageEntity, DataError> {
        val attachments = decodeAttachments(entity.attachments).toMutableList()
        val pendingIndices = attachments.indices.filter { attachments[it].url == null }
        if(pendingIndices.isEmpty()) {
            return Result.Success(entity)
        }

        val files = pendingIndices.map { index ->
            val localPath = attachments[index].localPath
                ?: return Result.Failure(DataError.Local.NOT_FOUND)
            attachmentFileStorage.read(localPath)
                ?: return Result.Failure(DataError.Local.NOT_FOUND)
        }

        val uploadUrls = when(
            val result = chatAttachmentService.getUploadUrls(
                chatId = entity.chatId,
                files = pendingIndices.map { index ->
                    ChatAttachmentService.AttachmentFile(
                        mimeType = attachments[index].mimeType,
                        fileName = attachments[index].fileName
                    )
                }
            )
        ) {
            is Result.Failure -> return result
            is Result.Success -> result.data
        }

        val totalBytes = files.sumOf { it.size.toLong() }.coerceAtLeast(1L)
        var uploadedBytes = 0L
        var currentEntity = entity
        uploadProgress.update { it + (entity.messageId to 0f) }

        try {
            pendingIndices.forEachIndexed { fileIndex, attachmentIndex ->
                val bytes = files[fileIndex]
                val uploadUrl = uploadUrls[fileIndex]

                val uploadResult = chatAttachmentService.upload(
                    uploadUrl = uploadUrl,
                    bytes = bytes,
                    mimeType = attachments[attachmentIndex].mimeType,
                    onProgress = { bytesSent ->
                        val progress = (uploadedBytes + bytesSent).toFloat() / totalBytes
                        uploadProgress.update { it + (entity.messageId to progress) }
                    }
                )
                if(uploadResult is Result.Failure) {
                    return uploadResult
                }

                uploadedBytes += bytes.size
                attachments[attachmentIndex] = attachments[attachmentIndex].copy(
                    url = uploadUrl.publicUrl
                )
                currentEntity = currentEntity.copy(
                    attachments = encodeAttachments(attachments)
                )
                database.chatMessageDao.upsertMessage(currentEntity)
            }
        } finally {
            uploadProgress.update { it - entity.messageId }
        }

        return Result.Success(currentEntity)
    }

    private suspend fun markAsFailed(messageId: String) {
        database.chatMessageDao.updateDeliveryStatus(
            messageId = messageId,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            status = ChatMessageDeliveryStatus.FAILED.name
        )
    }

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> {
        return chatMessageService
            .deleteMessage(messageId)
            .onSuccess {
                applicationScope.launch {
                    database.chatMessageDao.deleteMessageById(messageId)
                }.join()
            }
    }

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            database.chatMessageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError> {
        val isLocalUserRemoved = database.chatDao.getChatById(chatId)?.chat?.isLocalUserRemoved == true
        if(isLocalUserRemoved) {
            // Only the history from before the removal is kept, nothing new is loaded
            return Result.Success(emptyList())
        }

        return chatMessageService
            .fetchMessages(chatId, before)
            .onSuccess { messages ->
                return safeDatabaseUpdate {
                    database.chatMessageDao.upsertMessagesAndSyncIfNecessary(
                        chatId = chatId,
                        serverMessages = messages.map { it.toEntity() },
                        pageSize = ChatMessageConstants.PAGE_SIZE,
                        shouldSync = before == null // Only sync for most recent page
                    )
                    messages
                }
            }
    }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>> {
        return database
            .chatMessageDao
            .getMessagesByChatId(chatId)
            .map { messages ->
                messages.map { it.toDomain() }
            }
    }

    private fun OutgoingWebSocketDto.NewMessage.toJsonPayload(): String {
        val webSocketMessage = WebSocketMessageDto(
            type = type.name,
            payload = json.encodeToString(this)
        )
        return json.encodeToString(webSocketMessage)
    }
}