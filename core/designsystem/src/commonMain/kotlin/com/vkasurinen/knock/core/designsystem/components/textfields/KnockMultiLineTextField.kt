package com.vkasurinen.knock.core.designsystem.components.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.components.buttons.floatingSurface
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pill shaped multi-line input ("Composer / Input" in Figma) that floats on the chat wallpaper.
 */
@Composable
fun KnockMultiLineTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: () -> Unit = {},
    maxHeightInLines: Int = 5,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val textFieldFocusRequester = remember {
        FocusRequester()
    }
    val extended = MaterialTheme.colorScheme.extended
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .floatingSurface(
                shape = RoundedCornerShape(24.dp),
                borderColor = extended.surfaceOutline
            )
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {
                    textFieldFocusRequester.requestFocus()
                }
            )
            .padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            state = state,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = extended.textPrimary
            ),
            lineLimits = TextFieldLineLimits.MultiLine(
                minHeightInLines = 1,
                maxHeightInLines = maxHeightInLines
            ),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = {
                onKeyboardAction()
            },
            cursorBrush = SolidColor(extended.textPrimary),
            decorator = { innerBox ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if(placeholder != null && state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = extended.textPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    innerBox()
                }
            }
        )
        trailingIcon?.invoke()
    }
}

@Composable
@Preview
fun KnockMultiLineTextFieldPreview() {
    KnockTheme {
        KnockMultiLineTextField(
            state = rememberTextFieldState(
                initialText = "This is some text field content that maybe spans multiple lines",
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            placeholder = "Message",
        )
    }
}
