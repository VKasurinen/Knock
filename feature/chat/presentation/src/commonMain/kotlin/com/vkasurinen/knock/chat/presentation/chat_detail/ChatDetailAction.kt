package com.vkasurinen.knock.chat.presentation.chat_detail

import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.chat.presentation.profile.mediapicker.PickedImageData

sealed interface ChatDetailAction {
    data object OnSendMessageClick: ChatDetailAction
    data object OnScrollToTop: ChatDetailAction
    data class OnSelectChat(val chatId: String?): ChatDetailAction
    /** The app came back to the foreground with this chat open */
    data object OnScreenResumed: ChatDetailAction
    data object OnScreenPaused: ChatDetailAction
    data class OnDeleteMessageClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data class OnMessageLongClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data object OnDismissMessageMenu: ChatDetailAction
    data class OnRetryClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data object OnBackClick: ChatDetailAction
    data object OnChatOptionsClick: ChatDetailAction
    data object OnChatMembersClick: ChatDetailAction
    data class OnUserProfileClick(val userId: String): ChatDetailAction
    data object OnLeaveChatClick: ChatDetailAction
    data object OnConfirmLeaveChatClick: ChatDetailAction
    data object OnDismissLeaveChatConfirmation: ChatDetailAction
    data object OnDismissChatOptions: ChatDetailAction
    data object OnRetryPaginationClick: ChatDetailAction
    data object OnHideBanner: ChatDetailAction
    data class OnFirstVisibleIndexChanged(val index: Int): ChatDetailAction
    data class OnTopVisibleIndexChanged(val topVisibleIndex: Int): ChatDetailAction
    data object OnAttachImagesClick: ChatDetailAction
    class OnImagesPicked(val images: List<PickedImageData>): ChatDetailAction
    data class OnRemovePendingAttachment(val id: String): ChatDetailAction
    data class OnImageClick(val images: List<MessageAttachment>, val index: Int): ChatDetailAction
    data object OnDismissImageViewer: ChatDetailAction
    data class OnSaveImageClick(val image: MessageAttachment): ChatDetailAction
    /** Handled by the root, which asks for the microphone permission first */
    data object OnRecordVoiceMessageClick: ChatDetailAction
    data object OnStartRecording: ChatDetailAction
    data object OnCancelRecordingClick: ChatDetailAction
    data object OnSendRecordingClick: ChatDetailAction
    data class OnVoiceMessagePlayPauseClick(
        val messageId: String,
        val voiceMessage: MessageAttachment
    ): ChatDetailAction
    data class OnVoiceMessageSeek(
        val messageId: String,
        val voiceMessage: MessageAttachment,
        val positionMs: Long
    ): ChatDetailAction
}