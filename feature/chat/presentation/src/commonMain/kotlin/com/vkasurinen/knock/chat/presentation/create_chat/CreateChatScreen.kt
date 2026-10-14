package com.vkasurinen.knock.chat.presentation.create_chat

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import knock.feature.chat.presentation.generated.resources.create_chat
import knock.feature.chat.presentation.generated.resources.new_chat
import com.vkasurinen.knock.chat.presentation.components.ChatParticipantSearchTextSection
import com.vkasurinen.knock.chat.presentation.components.ChatParticipantsSelectionSection
import com.vkasurinen.knock.chat.presentation.components.ManageChatButtonSection
import com.vkasurinen.knock.chat.presentation.components.ManageChatHeaderRow
import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatAction
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatScreen
import com.vkasurinen.knock.core.designsystem.components.brand.KnockHorizontalDivider
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.dialogs.KnockAdaptiveDialogSheetLayout
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.presentation.util.DeviceConfiguration
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import com.vkasurinen.knock.core.presentation.util.clearFocusOnTap
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateChatRoot(
    onDismiss: () -> Unit,
    onChatCreated: (Chat) -> Unit,
    viewModel: CreateChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is CreateChatEvent.OnChatCreated -> onChatCreated(event.chat)
        }
    }

    KnockAdaptiveDialogSheetLayout(
        onDismiss = onDismiss
    ) {
        ManageChatScreen(
            headerText = stringResource(Res.string.new_chat),
            primaryButtonText = stringResource(Res.string.create_chat),
            state = state,
            onAction = { action ->
                when(action) {
                    ManageChatAction.OnDismissDialog -> onDismiss()
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}