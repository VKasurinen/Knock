package com.vkasurinen.knock

import com.vkasurinen.knock.windows.DeepLinkRequest
import com.vkasurinen.knock.windows.WindowState
import com.vkasurinen.knock.core.domain.preferences.ThemePreference

data class ApplicationState(
    val windows: List<WindowState> = listOf(WindowState()),
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    // Kept until the window opened it, so a link that arrives during startup isn't lost
    val pendingDeepLink: DeepLinkRequest? = null
)
