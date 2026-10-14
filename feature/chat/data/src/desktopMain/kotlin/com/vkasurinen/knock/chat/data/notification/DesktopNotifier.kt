package com.vkasurinen.knock.chat.data.notification

import com.vkasurinen.knock.chat.domain.chat.ChatConnectionClient
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull

/**
 * Desktop has no push notifications, so new messages from the open websocket connection are
 * turned into notifications while the app is running.
 */
class DesktopNotifier(
    private val chatConnectionClient: ChatConnectionClient,
    private val sessionStorage: SessionStorage,
    private val chatRepository: ChatRepository
) {
    data class NotificationPayload(
        val chatId: String,
        val title: String,
        val message: String
    )

    fun observeNewNotifications(): Flow<NotificationPayload> {
        return chatConnectionClient.chatMessages
            .mapNotNull { message ->
                val currentUserId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id
                if(message.senderId == currentUserId) {
                    return@mapNotNull null
                }

                val participants = chatRepository
                    .getChatInfoById(message.chatId)
                    .firstOrNull()
                    ?.chat
                    ?.participants
                    .orEmpty()

                val senderName = participants.find { it.userId == message.senderId }?.username
                // The other participants, so a group chat shows everyone in it
                val title = participants
                    .filter { it.userId != currentUserId }
                    .sortedBy { it.username }
                    .joinToString(", ") { it.username }
                    .ifEmpty { senderName ?: "Knock" }

                NotificationPayload(
                    chatId = message.chatId,
                    title = title,
                    message = if(senderName != null) {
                        "$senderName: ${message.previewText()}"
                    } else {
                        message.previewText()
                    }
                )
            }
    }

    /**
     * Messages with only images or a voice message have no text to show.
     */
    private fun ChatMessage.previewText(): String {
        if(content.isNotBlank()) {
            return content
        }
        val images = attachments.count { it.type == AttachmentType.IMAGE }
        return when {
            attachments.any { it.type == AttachmentType.AUDIO } -> "Voice message"
            images == 1 -> "Photo"
            images > 1 -> "$images photos"
            else -> content
        }
    }
}
