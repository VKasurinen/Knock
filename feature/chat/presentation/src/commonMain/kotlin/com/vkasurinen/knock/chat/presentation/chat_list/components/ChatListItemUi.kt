package com.vkasurinen.knock.chat.presentation.chat_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.chat.presentation.util.DateUtils
import com.vkasurinen.knock.chat.presentation.util.previewText
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.designsystem.components.avatar.KnockStackedAvatars
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.group_chat
import knock.feature.chat.presentation.generated.resources.only_you
import knock.feature.chat.presentation.generated.resources.you_were_removed_from_chat
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Chat list row as a card ("Chat List Item" in Figma). Selected: 2 dp btn/primary outline.
 */
@Composable
fun ChatListItemUi(
    chat: ChatUi,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    unreadCount: Int? = null
) {
    val extended = MaterialTheme.colorScheme.extended
    val isGroupChat = chat.isGroup
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .shadow(1.dp, shape, ambientColor = CardShadowColor, spotColor = CardShadowColor)
            .clip(shape)
            .background(extended.surfaceContainer)
            .border(
                width = if(isSelected) 2.dp else 1.dp,
                color = if(isSelected) MaterialTheme.colorScheme.primary else extended.surfaceOutline,
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if(chat.otherParticipants.isNotEmpty()) {
            KnockStackedAvatars(
                avatars = chat.otherParticipants,
                modifier = Modifier.alpha(if(chat.isLocalUserRemoved) 0.5f else 1f)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if(!isGroupChat) {
                        chat.otherParticipants.firstOrNull()?.username
                            ?: stringResource(Res.string.only_you)
                    } else {
                        chat.name ?: stringResource(Res.string.group_chat)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = extended.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if(chat.lastMessage != null) {
                    Text(
                        text = DateUtils.formatChatListTime(chat.lastMessage.createdAt).asString(),
                        style = MaterialTheme.typography.labelXSmall,
                        color = if(unreadCount != null && unreadCount > 0) {
                            extended.tertiary
                        } else extended.textTertiary
                    )
                }
            }
            if(isGroupChat) {
                val usernames = remember(chat.otherParticipants) {
                    chat.otherParticipants.joinToString { it.username }
                }
                Text(
                    text = usernames,
                    style = MaterialTheme.typography.bodySmall,
                    color = extended.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(chat.isLocalUserRemoved) {
                    Text(
                        text = stringResource(Res.string.you_were_removed_from_chat),
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = extended.textDestructive,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else if(chat.lastMessage != null) {
                    val lastMessagePreview = chat.lastMessage.previewText()
                    val previewMessage = if(isGroupChat && chat.lastMessageSenderUsername != null) {
                        "${chat.lastMessageSenderUsername}: $lastMessagePreview"
                    } else lastMessagePreview
                    Text(
                        text = previewMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if(unreadCount != null && unreadCount > 0) {
                            extended.textPrimary
                        } else extended.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if(unreadCount != null && unreadCount > 0) {
                    UnreadBadge(count = unreadCount)
                }
            }
        }
    }
}

/**
 * Unread counter. Not shown yet: the backend doesn't track read state.
 */
@Composable
private fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = count.toString(),
        style = MaterialTheme.typography.labelXSmall,
        color = MaterialTheme.colorScheme.extended.onAccentBrand,
        modifier = modifier
            .background(MaterialTheme.colorScheme.extended.accentBrand, RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

private val CardShadowColor = Color(0x143B291F)

@Composable
@Preview
fun ChatListItemUiPreview() {
    KnockTheme(darkTheme = true) {
        ChatListItemUi(
            isSelected = true,
            modifier = Modifier
                .fillMaxWidth(),
            chat = ChatUi(
                id = "1",
                localParticipant = ChatParticipantUi(
                    id = "1",
                    username = "Philipp",
                    initials = "PH",
                ),
                isGroup = true,
                name = "Kahvikerho",
                otherParticipants = listOf(
                    ChatParticipantUi(
                        id = "2",
                        username = "Cinderella",
                        initials = "CI",
                    ),
                    ChatParticipantUi(
                        id = "3",
                        username = "Josh",
                        initials = "JO",
                    )
                ),
                lastMessage = ChatMessage(
                    id = "1",
                    chatId = "1",
                    content = "This is a last chat message that was sent by Philipp " +
                            "and goes over multiple lines to showcase the ellipsis",
                    createdAt = Clock.System.now(),
                    senderId = "1",
                    deliveryStatus = ChatMessageDeliveryStatus.SENT
                ),
                lastMessageSenderUsername = "Philipp"
            )
        )
    }
}