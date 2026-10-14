package com.vkasurinen.knock.core.designsystem.components.textfields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall

@Composable
fun KnockTextFieldLayout(
    title: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    textField: @Composable (Modifier, MutableInteractionSource) -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
    }

    val extended = MaterialTheme.colorScheme.extended
    val shape = RoundedCornerShape(12.dp)
    val textFieldStyleModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .background(
            color = if(enabled) MaterialTheme.colorScheme.surface else extended.disabledFill,
            shape = shape
        )
        .border(
            width = when {
                isError -> 1.5.dp
                isFocused && enabled -> 2.dp
                else -> 1.dp
            },
            color = when {
                !enabled -> extended.disabledOutline
                isError -> extended.textDestructive
                isFocused -> MaterialTheme.colorScheme.primary
                else -> extended.surfaceOutline
            },
            shape = shape
        )
        .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp)

    Column(
        modifier = modifier
    ) {
        if(title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleXSmall,
                color = extended.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        textField(textFieldStyleModifier, interactionSource)

        if(supportingText != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = supportingText,
                color = when {
                    isError -> extended.textDestructive
                    !enabled -> extended.textDisabled
                    else -> extended.textTertiary
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}