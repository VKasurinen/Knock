package com.vkasurinen.knock.chat.presentation.components.manage_chat

import androidx.compose.foundation.text.input.TextFieldState
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.presentation.util.UiText

data class ManageChatState(
    val queryTextState: TextFieldState = TextFieldState(),
    val existingChatParticipants: List<ChatParticipantUi> = emptyList(),
    val selectedChatParticipants: List<ChatParticipantUi> = emptyList(),
    val isSearching: Boolean = false,
    val currentSearchResult: ChatParticipantUi? = null,
    val searchError: UiText? = null,
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null,
    val localUserId: String? = null,
    val adminUserId: String? = null,
    val canRemoveParticipants: Boolean = false,
    val participantPendingRemoval: ChatParticipantUi? = null,
    val isRemovingParticipant: Boolean = false,
    val groupNameTextState: TextFieldState = TextFieldState(),
    val showGroupName: Boolean = false,
    /** The group's name when the screen opened, to tell whether it was changed */
    val originalGroupName: String? = null,
) {
    val isSearchResultExistingMember: Boolean
        get() = currentSearchResult != null && existingChatParticipants.any { it.id == currentSearchResult.id }

    /** Trimmed group name, or null when it's blank */
    val normalizedGroupName: String?
        get() = groupNameTextState.text.toString().trim().takeIf { it.isNotEmpty() }

    val isGroupNameChanged: Boolean
        get() = showGroupName && normalizedGroupName != originalGroupName

    val canSubmit: Boolean
        get() = !isSubmitting && (selectedChatParticipants.isNotEmpty() || isGroupNameChanged)

    /** A search result that is already selected is shown in the selected list instead */
    val unselectedSearchResult: ChatParticipantUi?
        get() = currentSearchResult?.takeIf { result ->
            selectedChatParticipants.none { it.id == result.id }
        }
}
