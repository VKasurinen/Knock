package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.presentation.util.UiText
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MessageListItemUi(
    messageUi: MessageUi,
    messageWithOpenMenu: MessageUi.LocalUserMessage?,
    onMessageLongClick: (MessageUi.LocalUserMessage) -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: (MessageUi.LocalUserMessage) -> Unit,
    onRetryClick: (MessageUi.LocalUserMessage) -> Unit,
    playbackState: PlaybackState,
    attachmentCallbacks: AttachmentCallbacks,
    modifier: Modifier = Modifier,
    isGroupChat: Boolean = false
) {
    Box(
        modifier = modifier
    ) {
        when(messageUi) {
            is MessageUi.DateSeparator -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DateChip(date = messageUi.date.asString())
                }
            }
            is MessageUi.LocalUserMessage -> {
                LocalUserMessage(
                    message = messageUi,
                    messageWithOpenMenu = messageWithOpenMenu,
                    onMessageLongClick = { onMessageLongClick(messageUi) },
                    onDismissMessageMenu = onDismissMessageMenu,
                    onDeleteClick = { onDeleteClick(messageUi) },
                    onRetryClick = { onRetryClick(messageUi) },
                    playbackState = playbackState,
                    attachmentCallbacks = attachmentCallbacks
                )
            }
            is MessageUi.OtherUserMessage -> {
                OtherUserMessage(
                    message = messageUi,
                    isGroupChat = isGroupChat,
                    playbackState = playbackState,
                    attachmentCallbacks = attachmentCallbacks
                )
            }
        }
    }
}


@Composable
@Preview
fun MessageListItemLocalMessageUiPreview() {
    KnockTheme {
        MessageListItemUi(
            messageUi = MessageUi.LocalUserMessage(
                id = "1",
                content = "Hello world, this is a preview message that spans multiple lines",
                deliveryStatus = ChatMessageDeliveryStatus.SENT,
                formattedSentTime = UiText.DynamicString("Friday 2:20pm")
            ),
            messageWithOpenMenu = null,
            onRetryClick = {},
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            playbackState = PlaybackState(),
            attachmentCallbacks = AttachmentCallbacks({ _, _ -> }, { _, _ -> }, { _, _, _ -> }),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
@Preview
fun MessageListItemLocalMessageRetryUiPreview() {
    KnockTheme {
        MessageListItemUi(
            messageUi = MessageUi.LocalUserMessage(
                id = "1",
                content = "Hello world, this is a preview message that spans multiple lines",
                deliveryStatus = ChatMessageDeliveryStatus.FAILED,
                formattedSentTime = UiText.DynamicString("Friday 2:20pm")
            ),
            messageWithOpenMenu = null,
            onRetryClick = {},
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            playbackState = PlaybackState(),
            attachmentCallbacks = AttachmentCallbacks({ _, _ -> }, { _, _ -> }, { _, _, _ -> }),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
@Preview
fun MessageListItemOtherMessageUiPreview() {
    KnockTheme {
        MessageListItemUi(
            messageUi = MessageUi.OtherUserMessage(
                id = "1",
                content = "Hello world, this is a preview message that spans multiple lines",
                formattedSentTime = UiText.DynamicString("Friday 2:20pm"),
                sender = ChatParticipantUi(
                    id = "1",
                    username = "Philipp",
                    initials = "PH"
                )
            ),
            messageWithOpenMenu = null,
            onRetryClick = {},
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            playbackState = PlaybackState(),
            attachmentCallbacks = AttachmentCallbacks({ _, _ -> }, { _, _ -> }, { _, _, _ -> }),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}