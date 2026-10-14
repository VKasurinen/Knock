package com.vkasurinen.knock.windows

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import knock.desktopapp.generated.resources.Res
import knock.desktopapp.generated.resources.file
import knock.desktopapp.generated.resources.logo
import knock.desktopapp.generated.resources.new_window
import com.vkasurinen.knock.App
import com.vkasurinen.knock.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.stringResource
import java.awt.Dimension

@Composable
fun KnockWindow(
    appTheme: AppTheme,
    deepLinkRequest: DeepLinkRequest?,
    onDeepLinkHandled: (DeepLinkRequest) -> Unit,
    onCloseRequest: () -> Unit,
    onAddWindowClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    // Wide enough for the chat list and the chat side by side (840 dp and up)
    val windowState = rememberWindowState(
        width = 1200.dp,
        height = 800.dp
    )
    Window(
        onCloseRequest = onCloseRequest,
        state = windowState,
        title = "Knock",
        icon = painterResource(Res.drawable.logo)
    ) {
        window.minimumSize = Dimension(360, 480)

        FocusObserver(
            onFocusChanged = onFocusChanged
        )

        // Buffered, so a link waits until the app has checked the login and can navigate
        val deepLinks = remember { Channel<String>(Channel.BUFFERED) }
        val deepLinkFlow = remember(deepLinks) { deepLinks.receiveAsFlow() }
        LaunchedEffect(deepLinkRequest) {
            if(deepLinkRequest != null) {
                windowState.isMinimized = false
                window.bringToFront()
                deepLinks.send(deepLinkRequest.uri)
                onDeepLinkHandled(deepLinkRequest)
            }
        }

        MenuBar {
            Menu(
                text = stringResource(Res.string.file),
                mnemonic = 'F'
            ) {
                Item(
                    text = stringResource(Res.string.new_window),
                    mnemonic = 'N',
                    // Cmd + Shift + N on macOS, Ctrl + Shift + N elsewhere
                    shortcut = KeyShortcut(
                        key = Key.N,
                        ctrl = !isMacOs,
                        meta = isMacOs,
                        shift = true
                    ),
                    onClick = onAddWindowClick
                )
            }
        }

        App(
            isDarkTheme = appTheme == AppTheme.DARK,
            deepLinks = deepLinkFlow
        )
    }
}

private val isMacOs = System.getProperty("os.name").lowercase().contains("mac")

/**
 * Window managers don't let an app raise a window that isn't focused, so it's kept on top for a
 * moment. Without it the window may only blink in the taskbar.
 */
private fun ComposeWindow.bringToFront() {
    isAlwaysOnTop = true
    toFront()
    requestFocus()
    isAlwaysOnTop = false
}
