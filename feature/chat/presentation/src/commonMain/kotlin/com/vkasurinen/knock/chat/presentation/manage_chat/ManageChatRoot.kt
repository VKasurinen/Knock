package com.vkasurinen.knock.chat.presentation.manage_chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.chat_members
import knock.feature.chat.presentation.generated.resources.save
import knock.feature.chat.presentation.generated.resources.to_be_added
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatAction
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatScreen
import com.vkasurinen.knock.core.designsystem.components.dialogs.KnockAdaptiveDialogSheetLayout
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ManageChatRoot(
    chatId: String?,
    onDismiss: () -> Unit,
    onMembersAdded: () -> Unit,
    onMemberClick: (userId: String) -> Unit,
    viewModel: ManageChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(chatId) {
        viewModel.onAction(ManageChatAction.ChatParticipants.OnSelectChat(chatId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is ManageChatEvent.OnMembersAdded -> onMembersAdded()
        }
    }

    KnockAdaptiveDialogSheetLayout(
        onDismiss = onDismiss
    ) {
        ManageChatScreen(
            headerText = stringResource(Res.string.chat_members),
            primaryButtonText = stringResource(Res.string.save),
            selectedHeaderText = stringResource(Res.string.to_be_added),
            state = state,
            onAction = { action ->
                when(action) {
                    ManageChatAction.OnDismissDialog -> onDismiss()
                    is ManageChatAction.OnMemberClick -> onMemberClick(action.userId)
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}