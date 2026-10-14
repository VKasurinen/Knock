package com.vkasurinen.knock.core.designsystem.components.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vkasurinen.knock.core.presentation.util.currentDeviceConfiguration

/**
 * @param canDismiss when false, swiping, tapping outside or going back doesn't close the
 * dialog but calls [onDismissBlocked] instead, e.g. to ask about unsaved changes
 */
@Composable
fun KnockAdaptiveDialogSheetLayout(
    onDismiss: () -> Unit,
    canDismiss: Boolean = true,
    onDismissBlocked: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val configuration = currentDeviceConfiguration()
    if(configuration.isMobile) {
        KnockBottomSheet(
            onDismiss = onDismiss,
            canDismiss = canDismiss,
            onDismissBlocked = onDismissBlocked,
            content = content
        )
    } else {
        KnockDialogContent(
            onDismiss = {
                if(canDismiss) onDismiss() else onDismissBlocked()
            },
            content = content
        )
    }
}