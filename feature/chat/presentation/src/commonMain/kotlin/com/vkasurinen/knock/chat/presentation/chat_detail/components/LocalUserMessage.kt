package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.core.designsystem.components.chat.BubbleDirection
import com.vkasurinen.knock.core.designsystem.components.chat.KnockChatBubble
import com.vkasurinen.knock.core.designsystem.components.dropdown.DropDownItem
import com.vkasurinen.knock.core.designsystem.components.dropdown.KnockDropDownMenu
import com.vkasurinen.knock.core.designsystem.theme.extended
import knock.core.designsystem.generated.resources.trash_icon
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.delete_for_everyone
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import knock.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun LocalUserMessage(
    message: MessageUi.LocalUserMessage,
    messageWithOpenMenu: MessageUi.LocalUserMessage?,
    onMessageLongClick: () -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
    playbackState: PlaybackState,
    attachmentCallbacks: AttachmentCallbacks,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box {
            KnockChatBubble(
                messageContent = message.content,
                formattedDateTime = message.formattedSentTime.asString(),
                direction = BubbleDirection.OUT,
                messageStatus = {
                    MessageStatus(
                        status = message.deliveryStatus
                    )
                },
                onLongClick = {
                    onMessageLongClick()
                },
                attachmentContent = messageAttachmentContent(
                    messageId = message.id,
                    attachments = message.attachments,
                    playbackState = playbackState,
                    callbacks = attachmentCallbacks,
                    uploadProgress = message.uploadProgress
                )
            )

            KnockDropDownMenu(
                isOpen = message.id == messageWithOpenMenu?.id,
                onDismiss = onDismissMessageMenu,
                items = listOf(
                    DropDownItem(
                        title = stringResource(Res.string.delete_for_everyone),
                        icon = vectorResource(DesignSystemRes.drawable.trash_icon),
                        contentColor = MaterialTheme.colorScheme.extended.textDestructive,
                        onClick = onDeleteClick
                    ),
                )
            )
        }

        if(message.deliveryStatus == ChatMessageDeliveryStatus.FAILED) {
            MessageFailedHint(onRetryClick = onRetryClick)
        }
    }
}
