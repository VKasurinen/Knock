package com.vkasurinen.knock.chat.presentation.user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkasurinen.knock.chat.presentation.components.GroupListItem
import com.vkasurinen.knock.chat.presentation.profile.components.ProfileHeaderSection
import com.vkasurinen.knock.core.designsystem.components.avatar.AvatarSize
import com.vkasurinen.knock.core.designsystem.components.avatar.KnockAvatarPhoto
import com.vkasurinen.knock.core.designsystem.components.brand.KnockHorizontalDivider
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.dialogs.KnockAdaptiveDialogSheetLayout
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.common_groups
import knock.feature.chat.presentation.generated.resources.common_groups_count
import knock.feature.chat.presentation.generated.resources.create_group
import knock.feature.chat.presentation.generated.resources.member_since
import knock.feature.chat.presentation.generated.resources.month_names
import knock.feature.chat.presentation.generated.resources.no_common_groups
import knock.feature.chat.presentation.generated.resources.no_common_groups_desc
import knock.feature.chat.presentation.generated.resources.send_message
import knock.feature.chat.presentation.generated.resources.user_profile
import knock.feature.chat.presentation.generated.resources.users_icon
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun UserProfileRoot(
    userId: String?,
    onDismiss: () -> Unit,
    onOpenChat: (chatId: String) -> Unit,
    onCreateGroupWith: (userId: String) -> Unit,
    viewModel: UserProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.onAction(UserProfileAction.OnSelectUser(userId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is UserProfileEvent.OpenChat -> onOpenChat(event.chatId)
            is UserProfileEvent.CreateGroupWith -> onCreateGroupWith(event.userId)
        }
    }

    KnockAdaptiveDialogSheetLayout(
        onDismiss = onDismiss
    ) {
        UserProfileScreen(
            state = state,
            onAction = { action ->
                when(action) {
                    UserProfileAction.OnDismiss -> onDismiss()
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}

@Composable
fun UserProfileScreen(
    state: UserProfileState,
    onAction: (UserProfileAction) -> Unit,
) {
    val extended = MaterialTheme.colorScheme.extended
    val deviceConfiguration = currentDeviceConfiguration()
    Column(
        modifier = Modifier
            .then(
                // The bottom sheet on mobile is full height, the dialog is as tall as its content
                if(deviceConfiguration.isMobile) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ProfileHeaderSection(
            username = stringResource(Res.string.user_profile),
            subtitle = null,
            onCloseClick = { onAction(UserProfileAction.OnDismiss) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        )
        KnockHorizontalDivider()
        Column(
            modifier = Modifier
                .weight(1f, fill = deviceConfiguration.isMobile)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KnockAvatarPhoto(
                    displayText = state.participant?.initials.orEmpty(),
                    imageUrl = state.participant?.imageUrl,
                    size = AvatarSize.XLARGE
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.participant?.username.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    color = extended.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if(state.memberSince != null) {
                    Text(
                        text = formatMemberSince(state.memberSince),
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textTertiary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KnockButton(
                        text = stringResource(Res.string.send_message),
                        onClick = { onAction(UserProfileAction.OnSendMessageClick) },
                        isLoading = state.isOpeningChat,
                        modifier = Modifier.weight(1f)
                    )
                    KnockButton(
                        text = stringResource(Res.string.create_group),
                        onClick = { onAction(UserProfileAction.OnCreateGroupClick) },
                        style = KnockButtonStyle.SECONDARY,
                        modifier = Modifier.weight(1f)
                    )
                }
                if(state.error != null) {
                    Text(
                        text = state.error.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = extended.textDestructive
                    )
                }
            }

            KnockHorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if(state.commonGroups.isEmpty()) {
                        stringResource(Res.string.common_groups)
                    } else {
                        stringResource(Res.string.common_groups_count, state.commonGroups.size)
                    },
                    style = MaterialTheme.typography.labelXSmall,
                    color = extended.textTertiary
                )
                if(state.commonGroups.isEmpty()) {
                    NoCommonGroups(username = state.participant?.username.orEmpty())
                } else {
                    state.commonGroups.forEach { group ->
                        GroupListItem(
                            group = group,
                            onClick = { onAction(UserProfileAction.OnGroupClick(group.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoCommonGroups(username: String) {
    val extended = MaterialTheme.colorScheme.extended
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.users_icon),
            contentDescription = null,
            tint = extended.textTertiary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = stringResource(Res.string.no_common_groups),
            style = MaterialTheme.typography.titleXSmall,
            color = extended.textSecondary
        )
        Text(
            text = stringResource(Res.string.no_common_groups_desc, username),
            style = MaterialTheme.typography.bodySmall,
            color = extended.textTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun formatMemberSince(memberSince: Instant): String {
    val date = memberSince.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val monthNames = stringArrayResource(Res.array.month_names)
    val month = monthNames.getOrNull(date.month.ordinal) ?: date.month.ordinal.plus(1).toString()
    return stringResource(Res.string.member_since, month, date.year)
}
