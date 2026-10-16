package com.vkasurinen.knock.chat.presentation.components.manage_chat

import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi

sealed interface ManageChatAction {
    /** Selects a participant to add, or deselects one that is already selected */
    data class OnToggleParticipant(val participant: ChatParticipantUi): ManageChatAction
    data class OnMemberClick(val userId: String): ManageChatAction
    data object OnDismissDialog: ManageChatAction
    data object OnPrimaryActionClick: ManageChatAction
    data class OnRemoveParticipantClick(val participant: ChatParticipantUi): ManageChatAction
    data object OnConfirmRemoveParticipantClick: ManageChatAction
    data object OnDismissRemoveParticipant: ManageChatAction

    sealed interface ChatParticipants: ManageChatAction {
        data class OnSelectChat(val chatId: String?): ManageChatAction
        data class OnPreselectUsers(val userIds: List<String>): ManageChatAction
    }
}
