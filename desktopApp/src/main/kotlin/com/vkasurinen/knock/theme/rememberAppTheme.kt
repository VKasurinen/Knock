package com.vkasurinen.knock.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jthemedetecor.OsThemeDetector
import com.vkasurinen.knock.core.domain.preferences.ThemePreference
import java.util.function.Consumer

enum class AppTheme {
    LIGHT, DARK
}

/**
 * The theme picked in the tray menu, or the OS theme for [ThemePreference.SYSTEM]. Follows the
 * OS when it switches between light and dark while the app is running.
 */
@Composable
fun rememberAppTheme(
    themePreferenceFromAppSettings: ThemePreference
): AppTheme {
    var isSystemThemeDark by remember {
        if(OsThemeDetector.isSupported()) {
            mutableStateOf(OsThemeDetector.getDetector().isDark)
        } else {
            val isSettingsPreferenceDark = themePreferenceFromAppSettings == ThemePreference.DARK
            mutableStateOf(isSettingsPreferenceDark)
        }
    }

    DisposableEffect(Unit) {
        if(!OsThemeDetector.isSupported()) {
            return@DisposableEffect onDispose { }
        }
        // The detector calls back on its own thread, Compose state can be written from any thread
        val listener = Consumer<Boolean> { isDark -> isSystemThemeDark = isDark }
        OsThemeDetector.getDetector().registerListener(listener)

        onDispose {
            OsThemeDetector.getDetector().removeListener(listener)
        }
    }

    val isDarkTheme = when(themePreferenceFromAppSettings) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemThemeDark
    }

    return if(isDarkTheme) AppTheme.DARK else AppTheme.LIGHT
}
