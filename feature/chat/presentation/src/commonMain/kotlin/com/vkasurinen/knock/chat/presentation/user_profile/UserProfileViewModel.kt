@file:OptIn(ExperimentalCoroutinesApi::class)

package com.vkasurinen.knock.chat.presentation.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.chat.presentation.mappers.toUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import com.vkasurinen.knock.core.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val chatRepository: ChatRepository,
    private val chatParticipantService: ChatParticipantService,
    private val sessionStorage: SessionStorage
): ViewModel() {

    private val eventChannel = Channel<UserProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _userId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(UserProfileState())

    // The local database has the participant as soon as the sheet opens, the
    // remote profile only adds the join date
    private val localParticipant = _userId.flatMapLatest { userId ->
        if(userId != null) {
            chatRepository.getChatParticipant(userId).map { it?.toUi() }
        } else flowOf(null)
    }

    private val commonGroups = combine(
        _userId,
        sessionStorage.observeAuthInfo().map { it?.user?.id }
    ) { userId, localUserId -> userId to localUserId }
        .flatMapLatest { (userId, localUserId) ->
            if(userId != null && localUserId != null) {
                chatRepository.getCommonGroups(localUserId, userId).map { groups ->
                    groups.map { it.toUi(localUserId) }
                }
            } else flowOf(emptyList())
        }

    val state = combine(_state, localParticipant, commonGroups) { currentState, participant, groups ->
        currentState.copy(
            participant = participant ?: currentState.participant,
            commonGroups = groups
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = UserProfileState()
    )

    fun onAction(action: UserProfileAction) {
        when(action) {
            is UserProfileAction.OnSelectUser -> selectUser(action.userId)
            UserProfileAction.OnSendMessageClick -> openDirectChat()
            UserProfileAction.OnCreateGroupClick -> {
                val userId = _userId.value ?: return
                viewModelScope.launch {
                    eventChannel.send(UserProfileEvent.CreateGroupWith(userId))
                }
            }
            is UserProfileAction.OnGroupClick -> {
                viewModelScope.launch {
                    eventChannel.send(UserProfileEvent.OpenChat(action.chatId))
                }
            }
            UserProfileAction.OnDismiss -> Unit
        }
    }

    private fun selectUser(userId: String?) {
        if(userId == null || userId == _userId.value) {
            return
        }
        _userId.value = userId
        _state.value = UserProfileState()

        viewModelScope.launch {
            chatParticipantService
                .getUserProfile(userId)
                .onSuccess { profile ->
                    _state.update { it.copy(
                        memberSince = profile.memberSince,
                        participant = ChatParticipantUi(
                            id = profile.userId,
                            username = profile.username,
                            initials = profile.username.take(2).uppercase(),
                            imageUrl = profile.profilePictureUrl
                        )
                    ) }
                }
        }
    }

    private fun openDirectChat() {
        val userId = _userId.value ?: return
        if(state.value.isOpeningChat) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(
                isOpeningChat = true,
                error = null
            ) }

            chatRepository
                .getOrCreateDirectChat(userId)
                .onSuccess { chat ->
                    _state.update { it.copy(isOpeningChat = false) }
                    eventChannel.send(UserProfileEvent.OpenChat(chat.id))
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isOpeningChat = false,
                        error = error.toUiText()
                    ) }
                }
        }
    }
}
