package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.attach_images
import knock.feature.chat.presentation.generated.resources.cloud_off_icon
import knock.feature.chat.presentation.generated.resources.mic_icon
import knock.feature.chat.presentation.generated.resources.paperclip_icon
import knock.feature.chat.presentation.generated.resources.record_voice_message
import knock.feature.chat.presentation.generated.resources.send
import knock.feature.chat.presentation.generated.resources.send_a_message
import com.vkasurinen.knock.chat.domain.models.ConnectionState
import com.vkasurinen.knock.chat.presentation.model.PendingAttachmentUi
import com.vkasurinen.knock.chat.presentation.model.RecordingUi
import com.vkasurinen.knock.chat.presentation.util.toUiText
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockCircleButton
import com.vkasurinen.knock.core.designsystem.components.buttons.KnockGradientCircleButton
import knock.core.designsystem.generated.resources.send_icon
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import com.vkasurinen.knock.core.designsystem.components.textfields.KnockMultiLineTextField
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MessageBox(
    messageTextFieldState: TextFieldState,
    isSendButtonEnabled: Boolean,
    connectionState: ConnectionState,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    pendingAttachments: List<PendingAttachmentUi> = emptyList(),
    onAttachClick: () -> Unit = {},
    onRemoveAttachmentClick: (id: String) -> Unit = {},
    recording: RecordingUi? = null,
    onRecordClick: () -> Unit = {},
    onCancelRecordingClick: () -> Unit = {},
    onSendRecordingClick: () -> Unit = {}
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(visible = pendingAttachments.isNotEmpty()) {
            PendingAttachmentsRow(
                attachments = pendingAttachments,
                onRemoveClick = onRemoveAttachmentClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if(recording != null) {
            RecordingBar(
                recording = recording,
                onCancelClick = onCancelRecordingClick,
                onSendClick = onSendRecordingClick,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            MessageTextField(
                messageTextFieldState = messageTextFieldState,
                isSendButtonEnabled = isSendButtonEnabled,
                isConnected = isConnected,
                connectionState = connectionState,
                showRecordButton = messageTextFieldState.text.isBlank() && pendingAttachments.isEmpty(),
                onSendClick = onSendClick,
                onAttachClick = onAttachClick,
                onRecordClick = onRecordClick
            )
        }
    }
}

@Composable
private fun MessageTextField(
    messageTextFieldState: TextFieldState,
    isSendButtonEnabled: Boolean,
    isConnected: Boolean,
    connectionState: ConnectionState,
    showRecordButton: Boolean,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onRecordClick: () -> Unit
) {
    val canSend = isConnected && isSendButtonEnabled
    val extended = MaterialTheme.colorScheme.extended
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        KnockCircleButton(
            icon = vectorResource(Res.drawable.paperclip_icon),
            contentDescription = stringResource(Res.string.attach_images),
            onClick = onAttachClick,
            size = 48.dp,
            iconSize = 21.dp,
            iconTint = extended.tertiary
        )
        KnockMultiLineTextField(
            state = messageTextFieldState,
            modifier = Modifier
                .weight(1f)
                // Cmd + Enter on macOS, Ctrl + Enter elsewhere. Enter alone adds a new line.
                .onPreviewKeyEvent { keyEvent ->
                    val isModifierKeyPressed = keyEvent.isMetaPressed || keyEvent.isCtrlPressed
                    val isSendShortcutPressed = isModifierKeyPressed && keyEvent.key == Key.Enter

                    if(!isSendShortcutPressed) {
                        return@onPreviewKeyEvent false
                    }
                    // Same rule as the send button. The key up is consumed too, so no line break is added.
                    if(keyEvent.type == KeyEventType.KeyDown && canSend) {
                        onSendClick()
                    }
                    true
                },
            placeholder = if(isConnected) {
                stringResource(Res.string.send_a_message)
            } else {
                connectionState.toUiText().asString()
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send
            ),
            onKeyboardAction = onSendClick,
            trailingIcon = if(!isConnected) {
                {
                    Icon(
                        imageVector = vectorResource(Res.drawable.cloud_off_icon),
                        contentDescription = connectionState.toUiText().asString(),
                        modifier = Modifier.size(20.dp),
                        tint = extended.textTertiary
                    )
                }
            } else null
        )
        if(showRecordButton) {
            KnockGradientCircleButton(
                icon = vectorResource(Res.drawable.mic_icon),
                contentDescription = stringResource(Res.string.record_voice_message),
                onClick = onRecordClick,
                enabled = isConnected
            )
        } else {
            KnockGradientCircleButton(
                icon = vectorResource(DesignSystemRes.drawable.send_icon),
                contentDescription = stringResource(Res.string.send),
                onClick = onSendClick,
                enabled = canSend
            )
        }
    }
}

@Composable
@Preview
fun MessageBoxPreview() {
    KnockTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageBox(
                messageTextFieldState = rememberTextFieldState(),
                isSendButtonEnabled = true,
                connectionState = ConnectionState.CONNECTED,
                onSendClick = {},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}