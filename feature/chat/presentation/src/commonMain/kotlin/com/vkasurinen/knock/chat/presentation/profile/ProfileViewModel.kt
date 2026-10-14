package com.vkasurinen.knock.chat.presentation.profile

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.error_current_password_equal_to_new_one
import knock.feature.chat.presentation.generated.resources.error_current_password_incorrect
import knock.feature.chat.presentation.generated.resources.error_invalid_file_type
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantRepository
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.core.domain.auth.AuthService
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.preferences.AppLanguage
import com.vkasurinen.knock.core.domain.preferences.UserPreferences
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.validation.PasswordValidator
import com.vkasurinen.knock.core.presentation.util.UiText
import com.vkasurinen.knock.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authService: AuthService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val sessionStorage: SessionStorage,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ProfileEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(ProfileState())
    val state = combine(
        _state,
        sessionStorage.observeAuthInfo(),
        userPreferences.observeTypingIndicatorsEnabled(),
        userPreferences.observeAppLanguage()
    ) { currentState, authInfo, isTypingIndicatorsEnabled, appLanguage ->
        if(authInfo != null) {
            currentState.copy(
                appLanguage = appLanguage,
                username = authInfo.user.username,
                userInitials = authInfo.user.username.take(2),
                emailTextState = TextFieldState(initialText = authInfo.user.email),
                profilePictureUrl = authInfo.user.profilePictureUrl,
                isTypingIndicatorsEnabled = currentState.pendingTypingIndicatorsEnabled
                    ?: isTypingIndicatorsEnabled
            )
        } else currentState
    }
        .onStart {
            if (!hasLoadedInitialData) {
                observePasswordFields()
                fetchLocalParticipantDetails()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ProfileState()
        )

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.OnSaveClick -> save()
            is ProfileAction.OnDismiss -> requestClose()
            is ProfileAction.OnDiscardChangesClick -> close()
            is ProfileAction.OnKeepEditingClick -> {
                _state.update { it.copy(isDiscardChangesDialogVisible = false) }
            }
            is ProfileAction.OnToggleCurrentPasswordVisibility -> toggleCurrentPasswordVisibility()
            is ProfileAction.OnToggleNewPasswordVisibility -> toggleNewPasswordVisibility()
            is ProfileAction.OnPictureSelected -> selectPicture(action.bytes, action.mimeType)
            is ProfileAction.OnRemovePictureClick -> removePicture()
            is ProfileAction.OnToggleTypingIndicators -> toggleTypingIndicators(action.isEnabled)
            is ProfileAction.OnLanguageSelect -> selectLanguage(action.language)
            else -> Unit
        }
    }

    // The language applies right away, it is not part of the changes saved with the save button
    private fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            userPreferences.setAppLanguage(language)
        }
    }

    private fun requestClose() {
        if(state.value.hasUnsavedChanges) {
            _state.update { it.copy(isDiscardChangesDialogVisible = true) }
        } else {
            close()
        }
    }

    private fun close() {
        _state.update { it.copy(isDiscardChangesDialogVisible = false) }
        viewModelScope.launch {
            eventChannel.send(ProfileEvent.OnClose)
        }
    }

    private fun selectPicture(bytes: ByteArray, mimeType: String?) {
        if(mimeType == null) {
            _state.update { it.copy(
                imageError = UiText.Resource(Res.string.error_invalid_file_type)
            ) }
            return
        }

        _state.update { it.copy(
            pendingPicture = PendingProfilePicture(bytes = bytes, mimeType = mimeType),
            isPictureRemovalPending = false,
            imageError = null
        ) }
    }

    private fun removePicture() {
        _state.update {
            if(it.pendingPicture != null) {
                // Undo the picked picture, the current one stays
                it.copy(pendingPicture = null, imageError = null)
            } else {
                it.copy(isPictureRemovalPending = true, imageError = null)
            }
        }
    }

    private fun toggleTypingIndicators(isEnabled: Boolean) {
        viewModelScope.launch {
            val savedValue = userPreferences.observeTypingIndicatorsEnabled().first()
            _state.update { it.copy(
                pendingTypingIndicatorsEnabled = isEnabled.takeIf { enabled -> enabled != savedValue }
            ) }
        }
    }

    /**
     * Applies every pending change. Stops at the first failure, so whatever wasn't saved yet
     * stays pending and can be retried.
     */
    private fun save() {
        val currentState = state.value
        if(!currentState.canSave) {
            return
        }

        _state.update { it.copy(
            isSaving = true,
            imageError = null,
            newPasswordError = null
        ) }

        viewModelScope.launch {
            val pendingPicture = currentState.pendingPicture
            if(pendingPicture != null) {
                chatParticipantRepository
                    .uploadProfilePicture(
                        imageBytes = pendingPicture.bytes,
                        mimeType = pendingPicture.mimeType
                    )
                    .onFailure { error ->
                        _state.update { it.copy(imageError = error.toUiText(), isSaving = false) }
                        return@launch
                    }
                _state.update { it.copy(pendingPicture = null) }
            } else if(currentState.isPictureRemovalPending) {
                chatParticipantRepository
                    .deleteProfilePicture()
                    .onFailure { error ->
                        _state.update { it.copy(imageError = error.toUiText(), isSaving = false) }
                        return@launch
                    }
                _state.update { it.copy(isPictureRemovalPending = false) }
            }

            currentState.pendingTypingIndicatorsEnabled?.let { isEnabled ->
                userPreferences.setTypingIndicatorsEnabled(isEnabled)
                _state.update { it.copy(pendingTypingIndicatorsEnabled = null) }
            }

            if(currentState.isPasswordChangeRequested) {
                authService
                    .changePassword(
                        currentPassword = currentState.currentPasswordTextState.text.toString(),
                        newPassword = currentState.newPasswordTextState.text.toString()
                    )
                    .onFailure { error ->
                        val errorMessage = when(error) {
                            DataError.Remote.UNAUTHORIZED -> {
                                UiText.Resource(Res.string.error_current_password_incorrect)
                            }
                            DataError.Remote.CONFLICT -> {
                                UiText.Resource(Res.string.error_current_password_equal_to_new_one)
                            }
                            else -> error.toUiText()
                        }
                        _state.update { it.copy(newPasswordError = errorMessage, isSaving = false) }
                        return@launch
                    }
                currentState.currentPasswordTextState.clearText()
                currentState.newPasswordTextState.clearText()
            }

            _state.update { it.copy(isSaving = false) }
            eventChannel.send(ProfileEvent.OnSaved)
        }
    }

    private fun fetchLocalParticipantDetails() {
        viewModelScope.launch {
            chatParticipantRepository.fetchLocalParticipant()
        }
    }

    private fun toggleCurrentPasswordVisibility() {
        _state.update { it.copy(
            isCurrentPasswordVisible = !it.isCurrentPasswordVisible
        ) }
    }

    private fun toggleNewPasswordVisibility() {
        _state.update { it.copy(
            isNewPasswordVisible = !it.isNewPasswordVisible
        ) }
    }

    private fun observePasswordFields() {
        val currentPasswordFlow = snapshotFlow {
            state.value.currentPasswordTextState.text.toString()
        }.distinctUntilChanged()

        val newPasswordFlow = snapshotFlow {
            state.value.newPasswordTextState.text.toString()
        }.distinctUntilChanged()

        combine(
            currentPasswordFlow,
            newPasswordFlow
        ) { currentPassword, newPassword ->
            _state.update { it.copy(
                isPasswordChangeRequested = currentPassword.isNotBlank() || newPassword.isNotBlank(),
                canChangePassword = currentPassword.isNotBlank() &&
                        PasswordValidator.validate(newPassword).isValidPassword
            ) }
        }.launchIn(viewModelScope)
    }
}
