package com.vkasurinen.knock.chat.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextAlign
import com.vkasurinen.knock.core.designsystem.components.avatar.AvatarSize
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall
import knock.feature.chat.presentation.generated.resources.members
import knock.feature.chat.presentation.generated.resources.no_members
import knock.feature.chat.presentation.generated.resources.no_members_desc
import knock.feature.chat.presentation.generated.resources.users_icon
import knock.core.designsystem.generated.resources.close_icon
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.admin
import knock.feature.chat.presentation.generated.resources.remove_member
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.designsystem.components.avatar.KnockAvatarPhoto
import com.vkasurinen.knock.core.designsystem.components.brand.KnockHorizontalDivider
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall
import com.vkasurinen.knock.core.presentation.util.DeviceConfiguration
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColumnScope.ChatParticipantsSelectionSection(
    existingParticipants: List<ChatParticipantUi>,
    selectedParticipants: List<ChatParticipantUi>,
    modifier: Modifier = Modifier,
    searchResult: ChatParticipantUi? = null,
    adminUserId: String? = null,
    onRemoveParticipantClick: ((ChatParticipantUi) -> Unit)? = null
) {
    val deviceConfiguration = currentDeviceConfiguration()
    val rootHeightModifier = when(deviceConfiguration) {
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            Modifier
                .animateContentSize()
                .heightIn(min = 200.dp, max = 300.dp)
        }
        else -> Modifier
            .weight(1f)
    }

    Box(
        modifier = rootHeightModifier
            .then(modifier)
    ) {
        val isEmpty = existingParticipants.isEmpty() && selectedParticipants.isEmpty() && searchResult == null
        if(isEmpty) {
            NoMembersSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(vertical = 32.dp)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            searchResult?.let {
                item {
                    ChatParticipantListItem(
                        participantUi = searchResult,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            if(selectedParticipants.isNotEmpty() && searchResult == null) {
                items(
                    items = selectedParticipants,
                    key = { it.id }
                ) { participant ->
                    ChatParticipantListItem(
                        participantUi = participant,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            if(existingParticipants.isNotEmpty()) {
                if(selectedParticipants.isNotEmpty() || searchResult != null) {
                    item {
                        KnockHorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                item {
                    Text(
                        text = stringResource(Res.string.members),
                        style = MaterialTheme.typography.labelXSmall,
                        color = MaterialTheme.colorScheme.extended.textTertiary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }

            items(
                items = existingParticipants,
                key = { "existing_${it.id}"}
            ) { participant ->
                val isAdmin = participant.id == adminUserId
                ChatParticipantListItem(
                    participantUi = participant,
                    isAdmin = isAdmin,
                    onRemoveClick = if(onRemoveParticipantClick != null && !isAdmin) {
                        { onRemoveParticipantClick(participant) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun NoMembersSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.users_icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.textTertiary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.no_members),
            style = MaterialTheme.typography.titleXSmall,
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Text(
            text = stringResource(Res.string.no_members_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.extended.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun ChatParticipantListItem(
    participantUi: ChatParticipantUi,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    onRemoveClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KnockAvatarPhoto(
            displayText = participantUi.initials,
            imageUrl = participantUi.imageUrl,
            size = AvatarSize.XSMALL
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = participantUi.username,
                style = MaterialTheme.typography.titleXSmall,
                color = MaterialTheme.colorScheme.extended.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if(isAdmin) {
                Text(
                    text = stringResource(Res.string.admin),
                    style = MaterialTheme.typography.labelXSmall,
                    color = MaterialTheme.colorScheme.extended.tertiary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.extended.surfaceHigher,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        if(onRemoveClick != null) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemRes.drawable.close_icon),
                    contentDescription = stringResource(Res.string.remove_member),
                    tint = MaterialTheme.colorScheme.extended.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}