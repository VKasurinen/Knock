package com.vkasurinen.knock.chat.presentation.components.manage_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import knock.feature.chat.presentation.generated.resources.create_chat
import knock.feature.chat.presentation.generated.resources.group_name
import knock.feature.chat.presentation.generated.resources.group_name_desc
import knock.feature.chat.presentation.generated.resources.group_name_placeholder
import com.vkasurinen.knock.core.designsystem.components.textfields.KnockTextField
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardCapitalization
import knock.feature.chat.presentation.generated.resources.remove
import knock.feature.chat.presentation.generated.resources.remove_x_from_chat_desc
import knock.feature.chat.presentation.generated.resources.remove_x_from_chat_title
import com.vkasurinen.knock.chat.presentation.components.ChatParticipantSearchTextSection
import com.vkasurinen.knock.chat.presentation.components.ChatParticipantsSelectionSection
import com.vkasurinen.knock.chat.presentation.components.ManageChatButtonSection
import com.vkasurinen.knock.chat.presentation.components.ManageChatHeaderRow
import com.vkasurinen.knock.core.designsystem.components.brand.KnockHorizontalDivider
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.presentation.util.DeviceConfiguration
import com.vkasurinen.knock.core.presentation.util.clearFocusOnTap
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageChatScreen(
    headerText: String,
    primaryButtonText: String,
    selectedHeaderText: String,
    state: ManageChatState,
    onAction: (ManageChatAction) -> Unit,
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val isKeyboardVisible = imeHeight > 0
    val configuration = currentDeviceConfiguration()

    val shouldHideHeader = configuration == DeviceConfiguration.MOBILE_LANDSCAPE
            && (isKeyboardVisible || isTextFieldFocused)

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(
            visible = !shouldHideHeader
        ) {
            Column {
                ManageChatHeaderRow(
                    title = headerText,
                    onCloseClick = {
                        onAction(ManageChatAction.OnDismissDialog)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                KnockHorizontalDivider()
            }
        }
        AnimatedVisibility(visible = state.showGroupName) {
            KnockTextField(
                state = state.groupNameTextState,
                title = stringResource(Res.string.group_name),
                placeholder = stringResource(Res.string.group_name_placeholder),
                supportingText = stringResource(Res.string.group_name_desc),
                singleLine = true,
                capitalization = KeyboardCapitalization.Sentences,
                onFocusChanged = { isTextFieldFocused = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp)
            )
        }
        ChatParticipantSearchTextSection(
            queryState = state.queryTextState,
            isLoading = state.isSearching,
            modifier = Modifier
                .fillMaxWidth(),
            error = state.searchError,
            onFocusChanged = {
                isTextFieldFocused = it
            }
        )
        KnockHorizontalDivider()
        ChatParticipantsSelectionSection(
            existingParticipants = state.existingChatParticipants,
            selectedParticipants = state.selectedChatParticipants,
            selectedHeaderText = selectedHeaderText,
            onToggleParticipant = { participant ->
                onAction(ManageChatAction.OnToggleParticipant(participant))
            },
            modifier = Modifier
                .fillMaxWidth(),
            searchResult = state.unselectedSearchResult,
            isSearchResultExistingMember = state.isSearchResultExistingMember,
            adminUserId = state.adminUserId,
            localUserId = state.localUserId,
            onMemberClick = { userId ->
                onAction(ManageChatAction.OnMemberClick(userId))
            },
            onRemoveParticipantClick = if(state.canRemoveParticipants) {
                { participant -> onAction(ManageChatAction.OnRemoveParticipantClick(participant)) }
            } else null
        )
        KnockHorizontalDivider()
        ManageChatButtonSection(
            primaryButton = {
                KnockButton(
                    text = primaryButtonText,
                    onClick = {
                        onAction(ManageChatAction.OnPrimaryActionClick)
                    },
                    enabled = state.canSubmit,
                    isLoading = state.isSubmitting
                )
            },
            secondaryButton = {
                KnockButton(
                    text = stringResource(Res.string.cancel),
                    onClick = {
                        onAction(ManageChatAction.OnDismissDialog)
                    },
                    style = KnockButtonStyle.SECONDARY
                )
            },
            error = state.submitError?.asString(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    state.participantPendingRemoval?.let { participant ->
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.remove_x_from_chat_title, participant.username),
            description = stringResource(Res.string.remove_x_from_chat_desc, participant.username),
            confirmButtonText = stringResource(Res.string.remove),
            cancelButtonText = stringResource(Res.string.cancel),
            onConfirmClick = {
                onAction(ManageChatAction.OnConfirmRemoveParticipantClick)
            },
            onCancelClick = {
                onAction(ManageChatAction.OnDismissRemoveParticipant)
            },
            onDismiss = {
                onAction(ManageChatAction.OnDismissRemoveParticipant)
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    KnockTheme {
        ManageChatScreen(
            headerText = "Create chat",
            selectedHeaderText = "Selected",
            state = ManageChatState(),
            onAction = {},
            primaryButtonText = "Create chat"
        )
    }
}