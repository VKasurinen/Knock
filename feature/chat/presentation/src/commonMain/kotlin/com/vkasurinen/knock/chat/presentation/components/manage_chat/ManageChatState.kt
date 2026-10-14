package com.vkasurinen.knock.chat.presentation.components.manage_chat

import androidx.compose.foundation.text.input.TextFieldState
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.presentation.util.UiText

data class ManageChatState(
    val queryTextState: TextFieldState = TextFieldState(),
    val existingChatParticipants: List<ChatParticipantUi> = emptyList(),
    val selectedChatParticipants: List<ChatParticipantUi> = emptyList(),
    val isSearching: Boolean = false,
    val canAddParticipant: Boolean = false,
    val currentSearchResult: ChatParticipantUi? = null,
    val searchError: UiText? = null,
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null,
    val adminUserId: String? = null,
    val canRemoveParticipants: Boolean = false,
    val participantPendingRemoval: ChatParticipantUi? = null,
    val isRemovingParticipant: Boolean = false
)