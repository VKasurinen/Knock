package com.vkasurinen.knock

import com.vkasurinen.knock.chat.data.notification.DesktopNotifier
import com.vkasurinen.knock.windows.DeepLinkRequest
import com.vkasurinen.knock.windows.WindowState
import com.vkasurinen.knock.core.domain.preferences.ThemePreference
import com.vkasurinen.knock.core.domain.preferences.ThemePreferences
import io.github.kdroidfilter.knotify.builder.ExperimentalNotificationsApi
import io.github.kdroidfilter.knotify.builder.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The open windows of the app and the app wide settings. Closing the last window exits the app.
 */
class ApplicationStateHolder(
    private val applicationScope: CoroutineScope,
    private val themePreferences: ThemePreferences,
    private val desktopNotifier: DesktopNotifier
) {

    @Volatile
    private var lastFocusedWindowId: String? = null

    private val _state = MutableStateFlow(ApplicationState())
    val state = _state
        .onStart {
            observeThemePreference()
            observeNewMessages()
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            _state.value
        )

    /**
     * Shows a native notification for a new message, unless one of the windows is focused and
     * the user sees the message already.
     */
    @OptIn(ExperimentalNotificationsApi::class)
    private fun observeNewMessages() {
        desktopNotifier
            .observeNewNotifications()
            .onEach { notificationPayload ->
                val isAppInBackground = _state.value.windows.none { it.isFocused }

                if(isAppInBackground) {
                    sendNotification(
                        title = notificationPayload.title,
                        message = notificationPayload.message,
                        onActivated = {
                            onDeepLink("knock://chat_detail/${notificationPayload.chatId}")
                        }
                    )
                }
            }
            .launchIn(applicationScope)
    }

    /**
     * Opens [uri] in the window that was used last, or the first one if none was focused yet.
     * Called from any thread: notification clicks, the OS and program arguments.
     */
    fun onDeepLink(uri: String) {
        _state.update { state ->
            val targetWindow = state.windows.find { it.id == lastFocusedWindowId }
                ?: state.windows.firstOrNull()
                ?: return@update state
            state.copy(
                pendingDeepLink = DeepLinkRequest(windowId = targetWindow.id, uri = uri)
            )
        }
    }

    fun onDeepLinkHandled(request: DeepLinkRequest) {
        _state.update { state ->
            // A newer link may have arrived in the meantime
            if(state.pendingDeepLink == request) state.copy(pendingDeepLink = null) else state
        }
    }

    private fun observeThemePreference() {
        themePreferences
            .observeThemePreference()
            .onEach { themePreference ->
                _state.update { it.copy(
                    themePreference = themePreference
                ) }
            }
            .launchIn(applicationScope)
    }

    fun onWindowFocusChanged(id: String, isFocused: Boolean) {
        if(isFocused) {
            lastFocusedWindowId = id
        }
        _state.update { it.copy(
            windows = it.windows.map { currentWindow ->
                if(currentWindow.id == id) {
                    currentWindow.copy(isFocused = isFocused)
                } else currentWindow
            }
        ) }
    }

    fun onThemePreferenceClick(themePreference: ThemePreference) {
        applicationScope.launch {
            themePreferences.updateThemePreference(themePreference)
        }
    }

    fun onAddWindowClick() {
        _state.update { it.copy(
            windows = it.windows + WindowState()
        ) }
    }

    fun onWindowCloseRequest(id: String) {
        _state.update { it.copy(
            windows = it.windows.filter { it.id != id }
        ) }
    }
}
