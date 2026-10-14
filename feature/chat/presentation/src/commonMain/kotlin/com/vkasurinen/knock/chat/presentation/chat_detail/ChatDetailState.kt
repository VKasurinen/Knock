package com.vkasurinen.knock.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.TextFieldState
import com.vkasurinen.knock.chat.domain.models.ConnectionState
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.chat.presentation.model.ImageViewerState
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.chat.presentation.model.PendingAttachmentUi
import com.vkasurinen.knock.core.presentation.util.UiText
data class ChatDetailState(
    val chatUi: ChatUi? = null,
    val isLoading: Boolean = false,
    val messages: List<MessageUi> = emptyList(),
    val error: UiText? = null,
    val messageTextFieldState: TextFieldState = TextFieldState(),
    val canSendMessage: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val paginationError: UiText? = null,
    val endReached: Boolean = false,
    val messageWithOpenMenu: MessageUi.LocalUserMessage? = null,
    val bannerState: BannerState = BannerState(),
    val isChatOptionsOpen: Boolean = false,
    val isNearBottom: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val typingUserIds: List<String> = emptyList(),
    val typingText: UiText? = null,
    val isLeaveChatConfirmationVisible: Boolean = false,
    val pendingAttachments: List<PendingAttachmentUi> = emptyList(),
    val imageViewer: ImageViewerState? = null
)

data class BannerState(
    val formattedDate: UiText? = null,
    val isVisible: Boolean = false
)