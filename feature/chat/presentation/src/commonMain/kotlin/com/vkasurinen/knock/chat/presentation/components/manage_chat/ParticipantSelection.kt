package com.vkasurinen.knock.chat.presentation.components.manage_chat

import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.chat.presentation.mappers.toUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import com.vkasurinen.knock.core.presentation.util.UiText
import com.vkasurinen.knock.core.presentation.util.toUiText
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.error_participant_not_found
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Selection and search shared by creating a chat and managing its members.
 */
fun MutableStateFlow<ManageChatState>.toggleParticipant(participant: ChatParticipantUi) {
    update { state ->
        val isExistingMember = state.existingChatParticipants.any { it.id == participant.id }
        val isSelected = state.selectedChatParticipants.any { it.id == participant.id }
        when {
            isExistingMember -> state
            isSelected -> state.copy(
                selectedChatParticipants = state.selectedChatParticipants.filter { it.id != participant.id }
            )
            else -> state.copy(
                selectedChatParticipants = state.selectedChatParticipants + participant
            )
        }
    }
}

suspend fun MutableStateFlow<ManageChatState>.searchParticipant(
    query: String,
    chatParticipantService: ChatParticipantService
) {
    if(query.isBlank()) {
        update { it.copy(
            currentSearchResult = null,
            searchError = null
        ) }
        return
    }

    update { it.copy(isSearching = true) }

    chatParticipantService
        .searchParticipant(query.trim())
        .onSuccess { participant ->
            update { it.copy(
                currentSearchResult = participant.toUi(),
                isSearching = false,
                searchError = null
            ) }
        }
        .onFailure { error ->
            val errorMessage = when(error) {
                DataError.Remote.NOT_FOUND -> UiText.Resource(Res.string.error_participant_not_found)
                else -> error.toUiText()
            }
            update { it.copy(
                searchError = errorMessage,
                isSearching = false,
                currentSearchResult = null
            ) }
        }
}
