package com.vkasurinen.knock.chat.presentation.model

import androidx.compose.runtime.Immutable
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.presentation.util.UiText

sealed class MessageUi(open val id: String) {
    @Immutable
    data class LocalUserMessage(
        override val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val formattedSentTime: UiText,
        val attachments: List<MessageAttachment> = emptyList(),
        /** 0..1 while the attachments are uploading, otherwise null */
        val uploadProgress: Float? = null
    ): MessageUi(id)

    @Immutable
    data class OtherUserMessage(
        override val id: String,
        val content: String,
        val formattedSentTime: UiText,
        val sender: ChatParticipantUi,
        val attachments: List<MessageAttachment> = emptyList()
    ): MessageUi(id)

    @Immutable
    data class DateSeparator(
        override val id: String,
        val date: UiText,
    ): MessageUi(id)
}