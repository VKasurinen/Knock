package com.vkasurinen.knock.core.designsystem.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.vkasurinen.knock.core.designsystem.theme.extended
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * The dialog is as tall as its content and only limited by the window, so on a large screen
 * nothing has to scroll. Content that is taller than the window should scroll itself.
 */
@Composable
fun KnockDialogContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = modifier
                // Keeps the dialog off the window edges when it fills the whole height
                .padding(vertical = 24.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.extended.surfaceOutline),
            shadowElevation = 24.dp
        ) {
            content()
        }
    }
}