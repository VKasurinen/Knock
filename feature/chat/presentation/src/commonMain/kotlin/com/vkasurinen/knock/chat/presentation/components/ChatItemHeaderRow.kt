package com.vkasurinen.knock.chat.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.group_chat
import knock.feature.chat.presentation.generated.resources.only_you
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.components.avatar.AvatarSize
import com.vkasurinen.knock.core.designsystem.components.avatar.KnockStackedAvatars
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatItemHeaderRow(
    chat: ChatUi,
    isGroupChat: Boolean,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.SMALL,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if(avatarSize == AvatarSize.XSMALL) 10.dp else 12.dp)
    ) {
        if(chat.otherParticipants.isNotEmpty()) {
            KnockStackedAvatars(
                avatars = chat.otherParticipants,
                size = avatarSize
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = if(!isGroupChat) {
                    chat.otherParticipants.firstOrNull()?.username
                        ?: stringResource(Res.string.only_you)
                } else {
                    stringResource(Res.string.group_chat)
                },
                style = MaterialTheme.typography.titleXSmall,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            if(isGroupChat && showSubtitle) {
                val formattedUsernames = remember(chat.otherParticipants) {
                    chat.otherParticipants.joinToString {
                        it.username
                    }
                }
                Text(
                    text = formattedUsernames,
                    color = MaterialTheme.colorScheme.extended.textTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}