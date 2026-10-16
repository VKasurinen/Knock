package com.vkasurinen.knock.chat.presentation.profile

import com.vkasurinen.knock.chat.presentation.chat_detail.components.ImageViewerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.vkasurinen.knock.core.designsystem.components.switches.KnockSegmentedButton
import com.vkasurinen.knock.core.domain.preferences.AppLanguage
import com.vkasurinen.knock.core.presentation.locale.LocalAppLocale
import knock.feature.chat.presentation.generated.resources.language
import knock.feature.chat.presentation.generated.resources.language_hint
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.cancel
import knock.feature.chat.presentation.generated.resources.chat_settings
import knock.feature.chat.presentation.generated.resources.contact_knock_support_change_email
import knock.feature.chat.presentation.generated.resources.current_password
import knock.feature.chat.presentation.generated.resources.delete
import knock.feature.chat.presentation.generated.resources.delete_profile_picture
import knock.feature.chat.presentation.generated.resources.delete_profile_picture_desc
import knock.feature.chat.presentation.generated.resources.discard
import knock.feature.chat.presentation.generated.resources.discard_changes_desc
import knock.feature.chat.presentation.generated.resources.discard_changes_title
import knock.feature.chat.presentation.generated.resources.drag_picture_hint
import knock.feature.chat.presentation.generated.resources.email
import knock.feature.chat.presentation.generated.resources.keep_editing
import knock.feature.chat.presentation.generated.resources.new_password
import knock.feature.chat.presentation.generated.resources.password
import knock.feature.chat.presentation.generated.resources.password_change_successful
import knock.feature.chat.presentation.generated.resources.password_hint
import knock.feature.chat.presentation.generated.resources.profile_image
import knock.feature.chat.presentation.generated.resources.save
import knock.feature.chat.presentation.generated.resources.show_typing_indicators
import knock.feature.chat.presentation.generated.resources.show_typing_indicators_desc
import knock.feature.chat.presentation.generated.resources.upload_icon
import knock.feature.chat.presentation.generated.resources.upload_image
import com.vkasurinen.knock.chat.presentation.profile.components.DragAndDropOverlay
import com.vkasurinen.knock.chat.presentation.profile.components.ProfileHeaderSection
import com.vkasurinen.knock.chat.presentation.profile.components.ProfileSectionLayout
import com.vkasurinen.knock.chat.presentation.profile.mediapicker.isDragAndDropSupported
import com.vkasurinen.knock.chat.presentation.profile.mediapicker.rememberDragAndDropTarget
import com.vkasurinen.knock.chat.presentation.profile.mediapicker.rememberImagePickerLauncher
import com.vkasurinen.knock.core.designsystem.components.avatar.AvatarSize
import com.vkasurinen.knock.core.designsystem.components.avatar.KnockAvatarPhoto
import com.vkasurinen.knock.core.designsystem.components.brand.KnockHorizontalDivider
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockButtonStyle
import com.vkasurinen.knock.core.designsystem.components.dialogs.KnockAdaptiveDialogSheetLayout
import com.vkasurinen.knock.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import com.vkasurinen.knock.core.designsystem.components.switches.KnockSwitch
import com.vkasurinen.knock.core.designsystem.components.textfields.KnockPasswordTextField
import com.vkasurinen.knock.core.designsystem.components.textfields.KnockTextField
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.presentation.util.ObserveAsEvents
import com.vkasurinen.knock.core.presentation.util.clearFocusOnTap
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoot(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when(event) {
            ProfileEvent.OnSaved,
            ProfileEvent.OnClose -> onDismiss()
        }
    }

    val launcher = rememberImagePickerLauncher { pickedImages ->
        val pickedImageData = pickedImages.first()
        viewModel.onAction(ProfileAction.OnPictureSelected(
            pickedImageData.bytes,
            pickedImageData.mimeType
        ))
    }

    KnockAdaptiveDialogSheetLayout(
        onDismiss = onDismiss,
        canDismiss = !state.hasUnsavedChanges,
        onDismissBlocked = {
            viewModel.onAction(ProfileAction.OnDismiss)
        }
    ) {
        ProfileScreen(
            state = state,
            onAction = { action ->
                when(action) {
                    is ProfileAction.OnUploadPictureClick -> {
                        launcher.launch()
                    }
                    else -> Unit
                }
                viewModel.onAction(action)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
) {
    var isHoveringWithFile by remember {
        mutableStateOf(false)
    }
    val dragAndDropTarget = rememberDragAndDropTarget(
        onHover = { isHovered ->
            isHoveringWithFile = isHovered
        },
        onDrop = { imageData ->
            onAction(ProfileAction.OnPictureSelected(
                bytes = imageData.bytes,
                mimeType = imageData.mimeType
            ))
        }
    )

    val deviceConfiguration = currentDeviceConfiguration()

    Box {
        Column(
            modifier = Modifier
                .clearFocusOnTap()
                .then(
                    // The bottom sheet on mobile is full height, the dialog is as tall as its content
                    if(deviceConfiguration.isMobile) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
                )
                .background(MaterialTheme.colorScheme.surface)
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dragAndDropTarget
                )
        ) {
            ProfileHeaderSection(
                username = state.username,
                onCloseClick = {
                    onAction(ProfileAction.OnDismiss)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
            )
            KnockHorizontalDivider()
            Column(
                modifier = Modifier
                    .weight(1f, fill = deviceConfiguration.isMobile)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileSectionLayout(
                    headerText = stringResource(Res.string.profile_image)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KnockAvatarPhoto(
                            displayText = state.userInitials,
                            size = AvatarSize.LARGE,
                            imageModel = state.displayedPicture,
                            // Opens the picture full screen; uploading is done with the button
                            onClick = state.displayedPicture?.let {
                                { onAction(ProfileAction.OnPictureClick) }
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        FlowRow(
                            modifier = Modifier
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KnockButton(
                                text = stringResource(Res.string.upload_image),
                                onClick = {
                                    onAction(ProfileAction.OnUploadPictureClick)
                                },
                                style = KnockButtonStyle.SECONDARY,
                                enabled = !state.isSaving
                            )
                            KnockButton(
                                text = stringResource(Res.string.delete),
                                onClick = {
                                    onAction(ProfileAction.OnRemovePictureClick)
                                },
                                style = KnockButtonStyle.DESTRUCTIVE_SECONDARY,
                                enabled = !state.isSaving && state.canRemovePicture
                            )
                        }
                    }

                    if(isDragAndDropSupported) {
                        Text(
                            text = stringResource(Res.string.drag_picture_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.extended.textTertiary
                        )
                    }

                    if(state.imageError != null) {
                        Text(
                            text = state.imageError.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.extended.textDestructive
                        )
                    }
                }
                KnockHorizontalDivider()
                ProfileSectionLayout(
                    headerText = stringResource(Res.string.language)
                ) {
                    val currentLocale = LocalAppLocale.current
                    val selectedLanguage = when(state.appLanguage) {
                        AppLanguage.SYSTEM -> if(currentLocale.startsWith("fi")) {
                            AppLanguage.FINNISH
                        } else AppLanguage.ENGLISH
                        else -> state.appLanguage
                    }
                    KnockSegmentedButton(
                        options = listOf(AppLanguage.FINNISH, AppLanguage.ENGLISH),
                        selected = selectedLanguage,
                        onSelect = { language ->
                            onAction(ProfileAction.OnLanguageSelect(language))
                        },
                        label = { language ->
                            when(language) {
                                AppLanguage.FINNISH -> "Suomi"
                                else -> "English"
                            }
                        }
                    )
                }
                KnockHorizontalDivider()
                ProfileSectionLayout(
                    headerText = stringResource(Res.string.email)
                ) {
                    KnockTextField(
                        state = state.emailTextState,
                        enabled = false,
                        supportingText = stringResource(Res.string.contact_knock_support_change_email)
                    )
                }
                KnockHorizontalDivider()
                ProfileSectionLayout(
                    headerText = stringResource(Res.string.chat_settings)
                ) {
                    KnockSwitch(
                        checked = state.isTypingIndicatorsEnabled,
                        onCheckedChange = { isEnabled ->
                            onAction(ProfileAction.OnToggleTypingIndicators(isEnabled))
                        },
                        title = stringResource(Res.string.show_typing_indicators),
                        description = stringResource(Res.string.show_typing_indicators_desc)
                    )
                }
                KnockHorizontalDivider()
                KnockPasswordTextField(
                    state = state.currentPasswordTextState,
                    isPasswordVisible = state.isCurrentPasswordVisible,
                    onToggleVisibilityClick = {
                        onAction(ProfileAction.OnToggleCurrentPasswordVisibility)
                    },
                    title = stringResource(Res.string.current_password),
                    placeholder = stringResource(Res.string.current_password),
                    isError = state.newPasswordError != null,
                )
                KnockPasswordTextField(
                    state = state.newPasswordTextState,
                    isPasswordVisible = state.isNewPasswordVisible,
                    onToggleVisibilityClick = {
                        onAction(ProfileAction.OnToggleNewPasswordVisibility)
                    },
                    title = stringResource(Res.string.new_password),
                    placeholder = stringResource(Res.string.new_password),
                    isError = state.newPasswordError != null,
                    supportingText = state.newPasswordError?.asString()
                        ?: stringResource(Res.string.password_hint)
                )
            }
            KnockHorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                KnockButton(
                    text = stringResource(Res.string.cancel),
                    style = KnockButtonStyle.SECONDARY,
                    onClick = {
                        onAction(ProfileAction.OnDismiss)
                    }
                )
                KnockButton(
                    text = stringResource(Res.string.save),
                    onClick = {
                        onAction(ProfileAction.OnSaveClick)
                    },
                    enabled = state.canSave,
                    isLoading = state.isSaving
                )
            }
        }

        if(isHoveringWithFile) {
            // Covers the profile content only, without stretching the dialog
            DragAndDropOverlay(modifier = Modifier.matchParentSize())
        }
    }

    val displayedPicture = state.displayedPicture
    if(state.isPictureOpen && displayedPicture != null) {
        ImageViewerDialog(
            model = displayedPicture,
            contentDescription = state.username,
            onDismiss = { onAction(ProfileAction.OnDismissPicture) }
        )
    }

    if(state.isDiscardChangesDialogVisible) {
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.discard_changes_title),
            description = stringResource(Res.string.discard_changes_desc),
            confirmButtonText = stringResource(Res.string.discard),
            cancelButtonText = stringResource(Res.string.keep_editing),
            onConfirmClick = {
                onAction(ProfileAction.OnDiscardChangesClick)
            },
            onCancelClick = {
                onAction(ProfileAction.OnKeepEditingClick)
            },
            onDismiss = {
                onAction(ProfileAction.OnKeepEditingClick)
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    KnockTheme {
        ProfileScreen(
            state = ProfileState(),
            onAction = {}
        )
    }
}