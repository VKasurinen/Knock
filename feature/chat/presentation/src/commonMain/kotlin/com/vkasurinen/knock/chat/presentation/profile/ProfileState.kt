package com.vkasurinen.knock.chat.presentation.profile

import androidx.compose.foundation.text.input.TextFieldState
import com.vkasurinen.knock.core.domain.preferences.AppLanguage
import com.vkasurinen.knock.core.presentation.util.UiText

/**
 * Changes made in the dialog are only applied when the user presses save.
 */
data class ProfileState(
    val username: String = "",
    val userInitials: String = "--",
    val profilePictureUrl: String? = null,
    val pendingPicture: PendingProfilePicture? = null,
    val isPictureRemovalPending: Boolean = false,
    val imageError: UiText? = null,
    val emailTextState: TextFieldState = TextFieldState(),
    val currentPasswordTextState: TextFieldState = TextFieldState(),
    val newPasswordTextState: TextFieldState = TextFieldState(),
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val newPasswordError: UiText? = null,
    /** Both password fields are filled in and the new password is valid */
    val canChangePassword: Boolean = false,
    /** Either password field has been filled in */
    val isPasswordChangeRequested: Boolean = false,
    /** What the switch shows: the pending value if there is one, otherwise the saved one */
    val isTypingIndicatorsEnabled: Boolean = true,
    val pendingTypingIndicatorsEnabled: Boolean? = null,
    /** Applied immediately when changed */
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isSaving: Boolean = false,
    val isDiscardChangesDialogVisible: Boolean = false,
    /** The profile picture is open full screen */
    val isPictureOpen: Boolean = false
) {
    /** The avatar preview: a newly picked picture, nothing if removal is pending, or the current one */
    val displayedPicture: Any?
        get() = when {
            pendingPicture != null -> pendingPicture.bytes
            isPictureRemovalPending -> null
            else -> profilePictureUrl
        }

    val canRemovePicture: Boolean
        get() = pendingPicture != null || (profilePictureUrl != null && !isPictureRemovalPending)

    val hasUnsavedChanges: Boolean
        get() = pendingPicture != null ||
                isPictureRemovalPending ||
                pendingTypingIndicatorsEnabled != null ||
                isPasswordChangeRequested

    val canSave: Boolean
        get() = !isSaving && hasUnsavedChanges && (!isPasswordChangeRequested || canChangePassword)
}

class PendingProfilePicture(
    val bytes: ByteArray,
    val mimeType: String
)
