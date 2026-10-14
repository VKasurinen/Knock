@file:OptIn(ExperimentalUuidApi::class, ExperimentalComposeUiApi::class)

package com.vkasurinen.knock.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import knock.feature.chat.presentation.generated.resources.leave_and_delete
import knock.feature.chat.presentation.generated.resources.leave_chat_as_admin_desc
import knock.feature.chat.presentation.generated.resources.leave_chat_as_admin_title
import knock.feature.chat.presentation.generated.resources.microphone_permission_required
import knock.feature.chat.presentation.generated.resources.no_chat_selected
import knock.feature.chat.presentation.generated.resources.select_a_chat
import knock.feature.chat.presentation.generated.resources.storage_permission_required
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.presentation.chat_detail.components.AttachmentCallbacks
import com.vkasurinen.knock.chat.presentation.chat_detail.components.ChatDetailHeader
import com.vkasurinen.knock.chat.presentation.chat_detail.components.DateChip
import com.vkasurinen.knock.chat.presentation.chat_detail.components.ImageViewerDialog
import com.vkasurinen.knock.chat.presentation.chat_detail.components.MessageBannerListener
import com.vkasurinen.knock.chat.presentation.chat_detail.components.MessageBox
import com.vkasurinen.knock.chat.presentation.chat_detail.components.MessageList
import com.vkasurinen.knock.chat.presentation.chat_detail.components.PaginationScrollListener
import com.vkasurinen.knock.chat.presentation.chat_detail.components.RemovedFromChatBanner
import com.vkasurinen.knock.chat.presentation.chat_detail.components.TypingIndicator
import com.vkasurinen.knock.core.designsystem.components.chat.KnockChatWallpaper
import androidx.compose.foundation.layout.widthIn
import com.vkasurinen.knock.chat.presentation.components.EmptyListSection
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.chat.presentation.model.RecordingUi
import com.vkasurinen.knock.chat.presentation.profile.mediapicker.rememberImagePickerLauncher
import com.vkasurinen.knock.chat.presentation.util.isStoragePermissionRequiredToSaveImages
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.presentation.permissions.Permission
import com.vkasurinen.knock.core.presentation.permissions.PermissionState
import com.vkasurinen.knock.core.presentation.permissions.rememberPermissionController
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import com.vkasurinen.knock.core.presentation.util.UiText
import com.vkasurinen.knock.core.presentation.util.clearFocusOnTap
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatDetailRoot(
    chatId: String?,
    isDetailPresent: Boolean,
    onBack: () -> Unit,
    onChatMembersClick: () -> Unit,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recording by viewModel.recording.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    val snackbarState = remember { SnackbarHostState() }
    // The image viewer is a dialog that covers the screen, so it shows its own snackbars
    val imageViewerSnackbarState = remember { SnackbarHostState() }
    val messageListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val permissionController = rememberPermissionController()

    val imagePickerLauncher = rememberImagePickerLauncher(
        maxItems = MAX_IMAGES_PER_PICK
    ) { images ->
        viewModel.onAction(ChatDetailAction.OnImagesPicked(images))
    }

    suspend fun showMessage(message: String) {
        if (state.imageViewer != null) {
            imageViewerSnackbarState.showSnackbar(message)
        } else {
            snackbarState.showSnackbar(message)
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ChatDetailEvent.OnChatLeft -> onBack()
            ChatDetailEvent.OnNewMessage -> {
                scope.launch {
                    messageListState.animateScrollToItem(0)
                }
            }

            is ChatDetailEvent.OnError -> {
                showMessage(event.error.asStringAsync())
            }

            is ChatDetailEvent.OnInfo -> {
                showMessage(event.message.asStringAsync())
            }
        }
    }

    LaunchedEffect(chatId) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(chatId))
    }

    LaunchedEffect(chatId) {
        if(chatId != null) {
            messageListState.scrollToItem(0)
        }
    }

    BackHandler(
        enabled = !isDetailPresent
    ) {
        scope.launch {
            // Add artificial delay to prevent detail back animation from showing
            // an unselected chat the moment we go back
            delay(300)
            viewModel.onAction(ChatDetailAction.OnSelectChat(null))
        }
        onBack()
    }

    ChatDetailScreen(
        state = state,
        messageListState = messageListState,
        isDetailPresent = isDetailPresent,
        onAction = { action ->
            when (action) {
                is ChatDetailAction.OnChatMembersClick -> onChatMembersClick()
                is ChatDetailAction.OnBackClick -> onBack()
                is ChatDetailAction.OnAttachImagesClick -> imagePickerLauncher.launch()
                is ChatDetailAction.OnRecordVoiceMessageClick -> {
                    scope.launch {
                        val permissionState = permissionController.requestPermission(Permission.RECORD_AUDIO)
                        if (permissionState == PermissionState.GRANTED) {
                            viewModel.onAction(ChatDetailAction.OnStartRecording)
                        } else {
                            showMessage(getString(Res.string.microphone_permission_required))
                        }
                    }
                    return@ChatDetailScreen
                }
                is ChatDetailAction.OnSaveImageClick -> {
                    if (isStoragePermissionRequiredToSaveImages) {
                        scope.launch {
                            val permissionState = permissionController.requestPermission(Permission.WRITE_STORAGE)
                            if (permissionState == PermissionState.GRANTED) {
                                viewModel.onAction(action)
                            } else {
                                showMessage(getString(Res.string.storage_permission_required))
                            }
                        }
                    } else {
                        viewModel.onAction(action)
                    }
                    return@ChatDetailScreen
                }
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarState = snackbarState,
        imageViewerSnackbarState = imageViewerSnackbarState,
        recording = recording,
        playbackState = playbackState,
        isChatSelected = chatId != null
    )
}

private const val MAX_IMAGES_PER_PICK = 10
private val MAX_CONVERSATION_WIDTH = 760.dp

@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    messageListState: LazyListState,
    isDetailPresent: Boolean,
    snackbarState: SnackbarHostState,
    imageViewerSnackbarState: SnackbarHostState = remember { SnackbarHostState() },
    recording: RecordingUi? = null,
    playbackState: PlaybackState = PlaybackState(),
    isChatSelected: Boolean = state.chatUi != null,
    onAction: (ChatDetailAction) -> Unit,
) {
    val attachmentCallbacks = remember(onAction) {
        AttachmentCallbacks(
            onImageClick = { images, index ->
                onAction(ChatDetailAction.OnImageClick(images, index))
            },
            onVoiceMessagePlayPauseClick = { messageId, voiceMessage ->
                onAction(ChatDetailAction.OnVoiceMessagePlayPauseClick(messageId, voiceMessage))
            },
            onVoiceMessageSeek = { messageId, voiceMessage, positionMs ->
                onAction(ChatDetailAction.OnVoiceMessageSeek(messageId, voiceMessage, positionMs))
            }
        )
    }

    val realMessageItemCount = remember(state.messages) {
        state
            .messages
            .filter { it is MessageUi.LocalUserMessage || it is MessageUi.OtherUserMessage }
            .size
    }

    LaunchedEffect(messageListState) {
        snapshotFlow {
            messageListState.firstVisibleItemIndex to messageListState.layoutInfo.totalItemsCount
        }.filter { (firstVisibleIndex, totalItemsCount) ->
            firstVisibleIndex >= 0 && totalItemsCount > 0
        }.collect { (firstVisibleItemIndex, _) ->
            onAction(ChatDetailAction.OnFirstVisibleIndexChanged(firstVisibleItemIndex))
        }
    }

    MessageBannerListener(
        lazyListState = messageListState,
        messages = state.messages,
        isBannerVisible = state.bannerState.isVisible,
        onShowBanner = { index ->
            onAction(ChatDetailAction.OnTopVisibleIndexChanged(index))
        },
        onHide = {
            onAction(ChatDetailAction.OnHideBanner)
        }
    )

    PaginationScrollListener(
        lazyListState = messageListState,
        itemCount = realMessageItemCount,
        isPaginationLoading = state.isPaginationLoading,
        isEndReached = state.endReached,
        onNearTop = {
            onAction(ChatDetailAction.OnScrollToTop)
        }
    )

    var headerHeight by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    val isGroupChat = (state.chatUi?.otherParticipants?.size ?: 0) > 1

    KnockChatWallpaper(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarState)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clearFocusOnTap()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.chatUi == null) {
                        // While a selected chat is still loading, show nothing rather than
                        // flashing the "no chat selected" state
                        if (!isChatSelected) {
                            EmptyListSection(
                                title = stringResource(Res.string.no_chat_selected),
                                description = stringResource(Res.string.select_a_chat),
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                    } else {
                        ChatDetailHeader(
                            chatUi = state.chatUi,
                            isDetailPresent = isDetailPresent,
                            isChatOptionsDropDownOpen = state.isChatOptionsOpen,
                            onChatOptionsClick = {
                                onAction(ChatDetailAction.OnChatOptionsClick)
                            },
                            onDismissChatOptions = {
                                onAction(ChatDetailAction.OnDismissChatOptions)
                            },
                            onManageChatClick = {
                                onAction(ChatDetailAction.OnChatMembersClick)
                            },
                            onLeaveChatClick = {
                                onAction(ChatDetailAction.OnLeaveChatClick)
                            },
                            onBackClick = {
                                onAction(ChatDetailAction.OnBackClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged {
                                    headerHeight = with(density) {
                                        it.height.toDp()
                                    }
                                }
                        )
                        // On wide screens the conversation column is centered with a max width
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = MAX_CONVERSATION_WIDTH)
                                .fillMaxWidth()
                        ) {
                            // A new list for every chat. In the same list, switching chats would animate
                            // every message out and the new chat's messages in (animateItem).
                            key(state.chatUi?.id) {
                                MessageList(
                                    messages = state.messages,
                                    messageWithOpenMenu = state.messageWithOpenMenu,
                                    listState = messageListState,
                                    isPaginationLoading = state.isPaginationLoading,
                                    paginationError = state.paginationError?.asString(),
                                    onMessageLongClick = { message ->
                                        onAction(ChatDetailAction.OnMessageLongClick(message))
                                    },
                                    onMessageRetryClick = { message ->
                                        onAction(ChatDetailAction.OnRetryClick(message))
                                    },
                                    onDismissMessageMenu = {
                                        onAction(ChatDetailAction.OnDismissMessageMenu)
                                    },
                                    onDeleteMessageClick = { message ->
                                        onAction(ChatDetailAction.OnDeleteMessageClick(message))
                                    },
                                    onRetryPaginationClick = {
                                        onAction(ChatDetailAction.OnRetryPaginationClick)
                                    },
                                    playbackState = playbackState,
                                    attachmentCallbacks = attachmentCallbacks,
                                    isGroupChat = isGroupChat,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }

                            TypingIndicator(
                                text = state.typingText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            )

                            val composerModifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .padding(
                                    top = 8.dp,
                                    bottom = 12.dp,
                                    start = 12.dp,
                                    end = 12.dp
                                )
                            if (state.chatUi?.isLocalUserRemoved == true) {
                                RemovedFromChatBanner(modifier = composerModifier)
                            } else {
                                MessageBox(
                                    messageTextFieldState = state.messageTextFieldState,
                                    isSendButtonEnabled = state.canSendMessage,
                                    connectionState = state.connectionState,
                                    onSendClick = {
                                        onAction(ChatDetailAction.OnSendMessageClick)
                                    },
                                    pendingAttachments = state.pendingAttachments,
                                    onAttachClick = {
                                        onAction(ChatDetailAction.OnAttachImagesClick)
                                    },
                                    onRemoveAttachmentClick = { id ->
                                        onAction(ChatDetailAction.OnRemovePendingAttachment(id))
                                    },
                                    recording = recording,
                                    onRecordClick = {
                                        onAction(ChatDetailAction.OnRecordVoiceMessageClick)
                                    },
                                    onCancelRecordingClick = {
                                        onAction(ChatDetailAction.OnCancelRecordingClick)
                                    },
                                    onSendRecordingClick = {
                                        onAction(ChatDetailAction.OnSendRecordingClick)
                                    },
                                    modifier = composerModifier
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.bannerState.isVisible,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = headerHeight + 8.dp),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if(state.bannerState.formattedDate != null) {
                        DateChip(
                            date = state.bannerState.formattedDate.asString()
                        )
                    }
                }
            }
        }
    }

    state.imageViewer?.let { imageViewer ->
        ImageViewerDialog(
            images = imageViewer.images,
            initialIndex = imageViewer.initialIndex,
            snackbarHostState = imageViewerSnackbarState,
            onSaveClick = { image ->
                onAction(ChatDetailAction.OnSaveImageClick(image))
            },
            onDismiss = {
                onAction(ChatDetailAction.OnDismissImageViewer)
            }
        )
    }

    if (state.isLeaveChatConfirmationVisible) {
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.leave_chat_as_admin_title),
            description = stringResource(Res.string.leave_chat_as_admin_desc),
            confirmButtonText = stringResource(Res.string.leave_and_delete),
            cancelButtonText = stringResource(Res.string.cancel),
            onConfirmClick = {
                onAction(ChatDetailAction.OnConfirmLeaveChatClick)
            },
            onCancelClick = {
                onAction(ChatDetailAction.OnDismissLeaveChatConfirmation)
            },
            onDismiss = {
                onAction(ChatDetailAction.OnDismissLeaveChatConfirmation)
            }
        )
    }
}

@Preview
@Composable
private fun ChatDetailEmptyPreview() {
    KnockTheme {
        ChatDetailScreen(
            state = ChatDetailState(),
            isDetailPresent = false,
            onAction = {},
            messageListState = rememberLazyListState(),
            snackbarState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun ChatDetailMessagesPreview() {
    KnockTheme(darkTheme = true) {
        ChatDetailScreen(
            messageListState = rememberLazyListState(),
            state = ChatDetailState(
                messageTextFieldState = rememberTextFieldState(
                    initialText = "This is a new message!"
                ),
                canSendMessage = true,
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
                messages = (1..20).map {
                    if (it % 2 == 0) {
                        MessageUi.LocalUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world!",
                            deliveryStatus = ChatMessageDeliveryStatus.SENT,
                            formattedSentTime = UiText.DynamicString("Friday, Aug 20")
                        )
                    } else {
                        MessageUi.OtherUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world!",
                            sender = ChatParticipantUi(
                                id = Uuid.random().toString(),
                                username = "John",
                                initials = "JO"
                            ),
                            formattedSentTime = UiText.DynamicString("Friday, Aug 20"),
                        )
                    }
                }
            ),
            isDetailPresent = true,
            onAction = {},
            snackbarState = remember { SnackbarHostState() }
        )
    }
}