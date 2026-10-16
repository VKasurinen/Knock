package com.vkasurinen.knock.chat.presentation.profile

import com.vkasurinen.knock.core.domain.preferences.AppLanguage

sealed interface ProfileAction {
    data object OnDismiss: ProfileAction
    data object OnUploadPictureClick: ProfileAction
    data object OnPictureClick: ProfileAction
    data object OnDismissPicture: ProfileAction
    class OnPictureSelected(val bytes: ByteArray, val mimeType: String?): ProfileAction
    data object OnRemovePictureClick: ProfileAction
    data object OnToggleCurrentPasswordVisibility: ProfileAction
    data object OnToggleNewPasswordVisibility: ProfileAction
    data class OnToggleTypingIndicators(val isEnabled: Boolean): ProfileAction
    data class OnLanguageSelect(val language: AppLanguage): ProfileAction
    data object OnSaveClick: ProfileAction
    data object OnDiscardChangesClick: ProfileAction
    data object OnKeepEditingClick: ProfileAction
}
