package com.vkasurinen.knock.chat.presentation.chat_list

import com.vkasurinen.knock.core.designsystem.components.switches.KnockSegmentedButton
import knock.feature.chat.presentation.generated.resources.filter_all
import knock.feature.chat.presentation.generated.resources.filter_direct
import knock.feature.chat.presentation.generated.resources.filter_groups
import knock.feature.chat.presentation.generated.resources.no_direct_chats
import knock.feature.chat.presentation.generated.resources.no_group_chats
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.vectorResource
import knock.core.designsystem.generated.resources.plus_icon
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import knock.feature.chat.presentation.generated.resources.chat_was_deleted
import knock.feature.chat.presentation.generated.resources.new_chat
import knock.feature.chat.presentation.generated.resources.do_you_want_to_logout
import knock.feature.chat.presentation.generated.resources.do_you_want_to_logout_desc
import knock.feature.chat.presentation.generated.resources.logout
import knock.feature.chat.presentation.generated.resources.no_chats
import knock.feature.chat.presentation.generated.resources.no_chats_subtitle
import com.vkasurinen.knock.chat.presentation.chat_list.components.ChatListHeader
import com.vkasurinen.knock.chat.presentation.chat_list.components.ChatListItemUi
import com.vkasurinen.knock.chat.presentation.components.EmptyListSection
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.components.brand.KnockHorizontalDivider
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockFloatingActionButton
import com.vkasurinen.knock.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.presentation.permissions.Permission
import com.vkasurinen.knock.core.presentation.permissions.rememberPermissionController
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatListRoot(
    selectedChatId: String?,
    onChatClick: (String?) -> Unit,
    onSuccessfulLogout: () -> Unit,
    onCreateChatClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedChatId) {
        viewModel.onAction(ChatListAction.OnSelectChat(selectedChatId))
    }

    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            is ChatListEvent.OnLogoutError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.error.asStringAsync()
                    )
                }
            }
            ChatListEvent.OnLogoutSuccess -> onSuccessfulLogout()
            ChatListEvent.OnChatDeleted -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = getString(Res.string.chat_was_deleted)
                    )
                }
            }
        }
    }

    ChatListScreen(
        state = state,
        onAction = { action ->
            when(action) {
                is ChatListAction.OnSelectChat -> onChatClick(action.chatId)
                ChatListAction.OnCreateChatClick -> onCreateChatClick()
                ChatListAction.OnProfileSettingsClick -> onProfileSettingsClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val permissionController = rememberPermissionController()
    LaunchedEffect(true) {
        permissionController.requestPermission(Permission.NOTIFICATIONS)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            KnockFloatingActionButton(
                onClick = {
                    onAction(ChatListAction.OnCreateChatClick)
                }
            ) {
                Icon(
                    imageVector = vectorResource(DesignSystemRes.drawable.plus_icon),
                    contentDescription = stringResource(Res.string.new_chat)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatListHeader(
                localParticipant = state.localParticipant,
                isUserMenuOpen = state.isUserMenuOpen,
                onUserAvatarClick = {
                    onAction(ChatListAction.OnUserAvatarClick)
                },
                onLogoutClick = {
                    onAction(ChatListAction.OnLogoutClick)
                },
                onDismissMenu = {
                    onAction(ChatListAction.OnDismissUserMenu)
                },
                onProfileSettingsClick = {
                    onAction(ChatListAction.OnProfileSettingsClick)
                }
            )
            when {
                state.isLoading -> {
                    // Chats come from the local database almost instantly, so a spinner
                    // would only flash. Keep the space empty instead.
                    Spacer(modifier = Modifier.weight(1f))
                }
                state.chats.isEmpty() -> {
                    EmptyListSection(
                        title = stringResource(Res.string.no_chats),
                        description = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(
                                horizontal = 8.dp
                            )
                    )
                }
                else -> {
                    KnockSegmentedButton(
                        options = ChatFilter.entries,
                        selected = state.filter,
                        onSelect = { onAction(ChatListAction.OnFilterChange(it)) },
                        label = { it.label() },
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                    )
                    if(state.visibleChats.isEmpty()) {
                        EmptyListSection(
                            title = stringResource(
                                if(state.filter == ChatFilter.GROUPS) Res.string.no_group_chats
                                else Res.string.no_direct_chats
                            ),
                            description = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                        return@Column
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.visibleChats,
                            key = { it.id }
                        ) { chatUi ->
                            ChatListItemUi(
                                chat = chatUi,
                                isSelected = chatUi.id == state.selectedChatId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onAction(ChatListAction.OnSelectChat(chatUi.id))
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    if(state.showLogoutConfirmation) {
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.do_you_want_to_logout),
            description = stringResource(Res.string.do_you_want_to_logout_desc),
            confirmButtonText = stringResource(Res.string.logout),
            cancelButtonText = stringResource(Res.string.cancel),
            onDismiss = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            },
            onCancelClick = {
                onAction(ChatListAction.OnDismissLogoutDialog)
            },
            onConfirmClick = {
                onAction(ChatListAction.OnConfirmLogout)
            },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    KnockTheme {
        ChatListScreen(
            state = ChatListState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Composable
private fun ChatFilter.label(): String {
    return stringResource(
        when(this) {
            ChatFilter.ALL -> Res.string.filter_all
            ChatFilter.DIRECT -> Res.string.filter_direct
            ChatFilter.GROUPS -> Res.string.filter_groups
        }
    )
}
