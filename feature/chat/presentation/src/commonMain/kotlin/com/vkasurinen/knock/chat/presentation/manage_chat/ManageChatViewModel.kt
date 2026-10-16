@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.vkasurinen.knock.chat.presentation.manage_chat

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.models.ChatType
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatAction
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatState
import com.vkasurinen.knock.chat.presentation.components.manage_chat.searchParticipant
import com.vkasurinen.knock.chat.presentation.components.manage_chat.toggleParticipant
import com.vkasurinen.knock.chat.presentation.mappers.toUi
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import com.vkasurinen.knock.core.presentation.util.toUiText
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatParticipantService: ChatParticipantService,
    private val sessionStorage: SessionStorage
): ViewModel() {

    private val _chatId = MutableStateFlow<String?>(null)

    private val eventChannel = Channel<ManageChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false
    private var groupNameLoadedForChatId: String? = null

    private val _state = MutableStateFlow(ManageChatState())

    private val chatFlow = _chatId
        .flatMapLatest { chatId ->
            if(chatId != null) {
                chatRepository.getChatInfoById(chatId).map { it.chat }
            } else flowOf(null)
        }
        .onEach { chat ->
            // Fill the name field once per chat, so later updates don't overwrite what's typed
            if(chat != null && chat.id != groupNameLoadedForChatId) {
                groupNameLoadedForChatId = chat.id
                _state.value.groupNameTextState.setTextAndPlaceCursorAtEnd(chat.name.orEmpty())
                _state.update { it.copy(originalGroupName = chat.name) }
            }
        }

    private val participantsFlow = _chatId
        .flatMapLatest { chatId ->
            if(chatId != null) {
                chatRepository.getActiveParticipantsByChatId(chatId)
            } else emptyFlow()
        }

    val state = combine(
        _state,
        participantsFlow,
        chatFlow,
        sessionStorage.observeAuthInfo().map { it?.user?.id }
    ) { currentState, participants, chat, localUserId ->
        val adminUserId = chat?.creatorId
        val isLocalUserAdmin = adminUserId != null && adminUserId == localUserId
        currentState.copy(
            existingChatParticipants = participants.map { it.toUi() },
            localUserId = localUserId,
            adminUserId = adminUserId,
            canRemoveParticipants = isLocalUserAdmin,
            showGroupName = chat?.type == ChatType.GROUP && isLocalUserAdmin
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                searchFlow.launchIn(viewModelScope)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ManageChatState()
        )

    private val searchFlow = snapshotFlow { _state.value.queryTextState.text.toString() }
        .debounce(SEARCH_DEBOUNCE)
        .onEach { query ->
            _state.searchParticipant(query, chatParticipantService)
        }

    fun onAction(action: ManageChatAction) {
        when(action) {
            is ManageChatAction.OnToggleParticipant -> {
                // Existing members are filtered out against the combined state
                if(state.value.existingChatParticipants.none { it.id == action.participant.id }) {
                    _state.toggleParticipant(action.participant)
                }
            }
            ManageChatAction.OnPrimaryActionClick -> save()
            is ManageChatAction.OnRemoveParticipantClick -> {
                _state.update { it.copy(participantPendingRemoval = action.participant) }
            }
            ManageChatAction.OnConfirmRemoveParticipantClick -> removePendingParticipant()
            ManageChatAction.OnDismissRemoveParticipant -> {
                _state.update { it.copy(participantPendingRemoval = null) }
            }
            is ManageChatAction.ChatParticipants.OnSelectChat -> {
                _chatId.update { action.chatId }
            }
            else -> Unit
        }
    }

    private fun removePendingParticipant() {
        val chatId = _chatId.value ?: return
        val participant = state.value.participantPendingRemoval ?: return

        viewModelScope.launch {
            _state.update { it.copy(
                isRemovingParticipant = true,
                submitError = null
            ) }

            chatRepository
                .removeParticipant(
                    chatId = chatId,
                    userId = participant.id
                )
                .onSuccess {
                    _state.update { it.copy(
                        isRemovingParticipant = false,
                        participantPendingRemoval = null
                    ) }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isRemovingParticipant = false,
                        participantPendingRemoval = null,
                        submitError = error.toUiText()
                    ) }
                }
        }
    }

    /** Adds the selected participants and renames the group, whichever of them changed */
    private fun save() {
        val chatId = _chatId.value ?: return
        val currentState = state.value
        if(!currentState.canSubmit) {
            return
        }

        val selectedUserIds = currentState.selectedChatParticipants.map { it.id }

        viewModelScope.launch {
            _state.update { it.copy(
                isSubmitting = true,
                submitError = null
            ) }

            if(currentState.isGroupNameChanged) {
                val renameResult = chatRepository
                    .renameChat(chatId, currentState.normalizedGroupName)
                    .onFailure { error ->
                        _state.update { it.copy(
                            isSubmitting = false,
                            submitError = error.toUiText()
                        ) }
                    }
                if(renameResult is Result.Failure) {
                    return@launch
                }
            }

            if(selectedUserIds.isNotEmpty()) {
                chatRepository
                    .addParticipantsToChat(
                        chatId = chatId,
                        userIds = selectedUserIds
                    )
                    .onSuccess {
                        _state.update { it.copy(isSubmitting = false) }
                        eventChannel.send(ManageChatEvent.OnMembersAdded)
                    }
                    .onFailure { error ->
                        _state.update { it.copy(
                            isSubmitting = false,
                            submitError = error.toUiText()
                        ) }
                    }
            } else {
                _state.update { it.copy(isSubmitting = false) }
                eventChannel.send(ManageChatEvent.OnMembersAdded)
            }
        }
    }

    private companion object {
        val SEARCH_DEBOUNCE = 500.milliseconds
    }
}
