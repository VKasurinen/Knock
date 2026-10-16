package com.vkasurinen.knock.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.group_chat
import knock.feature.chat.presentation.generated.resources.member_count
import knock.feature.chat.presentation.generated.resources.users_icon
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

/**
 * A group chat in a list, e.g. the groups in common on a user's profile ("Group Row" in Figma).
 */
@Composable
fun GroupListItem(
    group: ChatUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.colorScheme.extended
    // Includes the local user, who is always a member of the groups shown
    val memberCount = group.otherParticipants.size + 1
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(extended.surfaceHigher, CircleShape)
                .border(1.dp, extended.surfaceOutline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.users_icon),
                contentDescription = null,
                tint = extended.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = group.name ?: stringResource(Res.string.group_chat),
                style = MaterialTheme.typography.titleXSmall,
                color = extended.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(Res.plurals.member_count, memberCount, memberCount),
                style = MaterialTheme.typography.bodySmall,
                color = extended.textTertiary
            )
        }
    }
}
