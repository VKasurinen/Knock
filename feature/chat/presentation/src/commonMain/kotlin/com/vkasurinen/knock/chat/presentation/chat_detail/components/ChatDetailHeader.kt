package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import knock.core.designsystem.generated.resources.arrow_left_icon
import knock.core.designsystem.generated.resources.dots_icon
import knock.core.designsystem.generated.resources.log_out_icon
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.chat_members
import knock.feature.chat.presentation.generated.resources.delete_chat
import knock.feature.chat.presentation.generated.resources.go_back
import knock.feature.chat.presentation.generated.resources.leave_chat
import knock.feature.chat.presentation.generated.resources.open_chat_options_menu
import knock.feature.chat.presentation.generated.resources.show_profile
import knock.feature.chat.presentation.generated.resources.users_icon
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.presentation.components.ChatItemHeaderRow
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.designsystem.components.avatar.AvatarSize
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockCircleButton
import com.vkasurinen.knock.core.designsystem.components.buttons.floatingSurface
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.vkasurinen.knock.core.designsystem.components.dropdown.KnockDropDownMenu
import com.vkasurinen.knock.core.designsystem.components.dropdown.DropDownItem
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.Clock
import knock.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun ChatDetailHeader(
    chatUi: ChatUi?,
    isDetailPresent: Boolean,
    isChatOptionsDropDownOpen: Boolean,
    onChatOptionsClick: () -> Unit,
    onDismissChatOptions: ()  -> Unit,
    onManageChatClick: () -> Unit,
    onUserProfileClick: (userId: String) -> Unit,
    onLeaveChatClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if(!isDetailPresent) {
            KnockCircleButton(
                icon = vectorResource(DesignSystemRes.drawable.arrow_left_icon),
                contentDescription = stringResource(Res.string.go_back),
                onClick = onBackClick
            )
        }

        if(chatUi != null) {
            val isGroupChat = chatUi.isGroup
            val directChatUserId = chatUi.otherParticipants.firstOrNull()?.id
            ChatItemHeaderRow(
                chat = chatUi,
                isGroupChat = isGroupChat,
                avatarSize = AvatarSize.XSMALL,
                modifier = Modifier
                    .weight(1f)
                    .floatingSurface(RoundedCornerShape(24.dp))
                    .clickable(
                        enabled = !chatUi.isLocalUserRemoved && (isGroupChat || directChatUserId != null)
                    ) {
                        // Groups open the member list, direct chats the other user's profile
                        if(isGroupChat) onManageChatClick() else directChatUserId?.let(onUserProfileClick)
                    }
                    .padding(start = 8.dp, end = 18.dp, top = 6.dp, bottom = 6.dp)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Box {
            KnockCircleButton(
                icon = vectorResource(DesignSystemRes.drawable.dots_icon),
                contentDescription = stringResource(Res.string.open_chat_options_menu),
                onClick = onChatOptionsClick
            )

            KnockDropDownMenu(
                isOpen = isChatOptionsDropDownOpen,
                onDismiss = onDismissChatOptions,
                items = buildList {
                    val directChatUserId = chatUi
                        ?.takeIf { !it.isGroup }
                        ?.otherParticipants
                        ?.firstOrNull()
                        ?.id
                    if(chatUi?.isLocalUserRemoved != true && chatUi?.isGroup == true) {
                        add(
                            DropDownItem(
                                title = stringResource(Res.string.chat_members),
                                icon = vectorResource(Res.drawable.users_icon),
                                contentColor = MaterialTheme.colorScheme.extended.textPrimary,
                                onClick = onManageChatClick
                            )
                        )
                    } else if(chatUi?.isLocalUserRemoved != true && directChatUserId != null) {
                        add(
                            DropDownItem(
                                title = stringResource(Res.string.show_profile),
                                icon = vectorResource(Res.drawable.users_icon),
                                contentColor = MaterialTheme.colorScheme.extended.textPrimary,
                                onClick = { onUserProfileClick(directChatUserId) }
                            )
                        )
                    }
                    add(
                        DropDownItem(
                            title = if(chatUi?.isLocalUserRemoved == true) {
                                stringResource(Res.string.delete_chat)
                            } else {
                                stringResource(Res.string.leave_chat)
                            },
                            icon = vectorResource(DesignSystemRes.drawable.log_out_icon),
                            contentColor = MaterialTheme.colorScheme.extended.textDestructive,
                            onClick = onLeaveChatClick
                        )
                    )
                }
            )
        }
    }
}

@Composable
@Preview
fun ChatDetailHeaderPreview() {
    KnockTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box {
                ChatDetailHeader(
                    isDetailPresent = false,
                    isChatOptionsDropDownOpen = true,
                    chatUi = ChatUi(
                        id = "1",
                        localParticipant = ChatParticipantUi(
                            id = "1",
                            username = "Philipp",
                            initials = "PH",
                        ),
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
                    ),
                    onChatOptionsClick = {},
                    onManageChatClick = {},
                    onUserProfileClick = {},
                    onLeaveChatClick = {},
                    onDismissChatOptions = {},
                    onBackClick = {},
                )
            }
        }
    }
}