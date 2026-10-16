package com.vkasurinen.knock.chat.presentation.create_chat

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatAction
import com.vkasurinen.knock.chat.presentation.components.manage_chat.ManageChatState
import com.vkasurinen.knock.chat.presentation.components.manage_chat.searchParticipant
import com.vkasurinen.knock.chat.presentation.components.manage_chat.toggleParticipant
import com.vkasurinen.knock.chat.presentation.mappers.toUi
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import com.vkasurinen.knock.core.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class CreateChatViewModel(
    private val chatParticipantService: ChatParticipantService,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<CreateChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ManageChatState())

    private val searchFlow = snapshotFlow { _state.value.queryTextState.text.toString() }
        .debounce(SEARCH_DEBOUNCE)
        .onEach { query ->
            _state.searchParticipant(query, chatParticipantService)
        }

    val state = _state
        // Two or more other people make a group, which can have a name
        .map { it.copy(showGroupName = it.selectedChatParticipants.size >= 2) }
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

    fun onAction(action: ManageChatAction) {
        when (action) {
            is ManageChatAction.OnToggleParticipant -> _state.toggleParticipant(action.participant)
            is ManageChatAction.ChatParticipants.OnPreselectUsers -> preselectUsers(action.userIds)
            ManageChatAction.OnPrimaryActionClick -> createChat()
            else -> Unit
        }
    }

    private fun preselectUsers(userIds: List<String>) {
        viewModelScope.launch {
            userIds.forEach { userId ->
                val isSelected = _state.value.selectedChatParticipants.any { it.id == userId }
                val participant = chatRepository.getChatParticipant(userId).first()
                if(participant != null && !isSelected) {
                    _state.toggleParticipant(participant.toUi())
                }
            }
        }
    }

    private fun createChat() {
        val currentState = state.value
        val userIds = currentState.selectedChatParticipants.map { it.id }
        if(userIds.isEmpty() || currentState.isSubmitting) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(
                isSubmitting = true,
                submitError = null
            ) }

            chatRepository
                .createChat(
                    otherUserIds = userIds,
                    name = if(currentState.showGroupName) currentState.normalizedGroupName else null
                )
                .onSuccess { chat ->
                    _state.update { it.copy(
                        isSubmitting = false
                    ) }
                    eventChannel.send(CreateChatEvent.OnChatCreated(chat))
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        submitError = error.toUiText(),
                        isSubmitting = false
                    ) }
                }
        }
    }

    private companion object {
        val SEARCH_DEBOUNCE = 500.milliseconds
    }
}
