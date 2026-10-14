package com.vkasurinen.knock.chat.presentation.components.manage_chat

import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi

sealed interface ManageChatAction {
    data object OnAddClick: ManageChatAction
    data object OnDismissDialog: ManageChatAction
    data object OnPrimaryActionClick: ManageChatAction
    data class OnRemoveParticipantClick(val participant: ChatParticipantUi): ManageChatAction
    data object OnConfirmRemoveParticipantClick: ManageChatAction
    data object OnDismissRemoveParticipant: ManageChatAction

    sealed interface ChatParticipants: ManageChatAction {
        data class OnSelectChat(val chatId: String?): ManageChatAction
    }
}