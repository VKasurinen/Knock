package com.vkasurinen.knock.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.FrameWindowScope
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

@Composable
fun FrameWindowScope.FocusObserver(
    onFocusChanged: (Boolean) -> Unit
) {
    val currentOnFocusChanged by rememberUpdatedState(onFocusChanged)

    DisposableEffect(window) {
        val focusListener = object : WindowFocusListener {
            override fun windowGainedFocus(event: WindowEvent?) {
                currentOnFocusChanged(true)
            }

            override fun windowLostFocus(event: WindowEvent?) {
                currentOnFocusChanged(false)
            }
        }

        window.addWindowFocusListener(focusListener)
        // A window that is focused before the listener is added doesn't get the first event
        currentOnFocusChanged(window.isFocused)

        onDispose {
            window.removeWindowFocusListener(focusListener)
        }
    }
}
