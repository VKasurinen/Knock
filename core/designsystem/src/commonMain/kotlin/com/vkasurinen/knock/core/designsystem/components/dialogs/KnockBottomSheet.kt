@file:OptIn(ExperimentalMaterial3Api::class)

package com.vkasurinen.knock.core.designsystem.components.dialogs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.vkasurinen.knock.core.designsystem.theme.extended
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KnockBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    canDismiss: Boolean = true,
    onDismissBlocked: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // The sheet state is created once, so it needs the latest values
    val currentCanDismiss by rememberUpdatedState(canDismiss)
    val currentOnDismissBlocked by rememberUpdatedState(onDismissBlocked)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if(value == SheetValue.Hidden && !currentCanDismiss) {
                currentOnDismissBlocked()
                false
            } else true
        }
    )
    LaunchedEffect(sheetState.isVisible) {
        if(sheetState.isVisible) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.extended.overlay,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.extended.secondaryOutline,
                width = 36.dp,
                height = 4.dp
            )
        },
        contentWindowInsets = { WindowInsets(left = 0.dp) },
        modifier = modifier.statusBarsPadding(),
    ) {
        content()
    }
}