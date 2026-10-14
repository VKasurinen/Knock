package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.chat.presentation.util.getSenderColorForUser
import com.vkasurinen.knock.core.designsystem.components.avatar.AvatarSize
import com.vkasurinen.knock.core.designsystem.components.avatar.KnockAvatarPhoto
import com.vkasurinen.knock.core.designsystem.components.chat.BubbleDirection
import com.vkasurinen.knock.core.designsystem.components.chat.KnockChatBubble

@Composable
fun OtherUserMessage(
    message: MessageUi.OtherUserMessage,
    isGroupChat: Boolean,
    playbackState: PlaybackState,
    attachmentCallbacks: AttachmentCallbacks,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if(isGroupChat) {
            KnockAvatarPhoto(
                displayText = message.sender.initials,
                imageUrl = message.sender.imageUrl,
                size = AvatarSize.XSMALL
            )
        }
        KnockChatBubble(
            messageContent = message.content,
            formattedDateTime = message.formattedSentTime.asString(),
            direction = BubbleDirection.IN,
            senderName = if(isGroupChat) message.sender.username else null,
            senderColor = getSenderColorForUser(message.sender.id),
            attachmentContent = messageAttachmentContent(
                messageId = message.id,
                attachments = message.attachments,
                playbackState = playbackState,
                callbacks = attachmentCallbacks
            )
        )
    }
}
