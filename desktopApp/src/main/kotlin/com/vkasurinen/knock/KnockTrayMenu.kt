package com.vkasurinen.knock

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import knock.desktopapp.generated.resources.Res
import knock.desktopapp.generated.resources.app_theme
import com.kdroid.composetray.tray.api.Tray
import com.vkasurinen.knock.core.domain.preferences.ThemePreference
import org.jetbrains.compose.resources.stringResource
import java.io.File

/**
 * Uses the OS' own tray and menus (AppIndicator on Linux) instead of Compose's AWT tray, whose
 * menu looks outdated on Linux. Without a primary action, a left click opens the menu.
 */
@Composable
fun ApplicationScope.KnockTrayMenu(
    themePreferenceFromAppSettings: ThemePreference,
    onThemePreferenceClick: (ThemePreference) -> Unit
) {
    val appThemeText = stringResource(Res.string.app_theme)

    // The icon is passed as a file on purpose: ComposeNativeTray 1.3.3 renders painter and
    // composable icons with a Skia API that Compose 1.12 changed, which crashes on startup
    @Suppress("DEPRECATION")
    Tray(
        iconPath = TRAY_ICON_PNG,
        windowsIconPath = TRAY_ICON_ICO,
        tooltip = "Knock",
        menuContent = {
            SubMenu(label = appThemeText) {
                ThemePreference.entries.forEach { themePreference ->
                    CheckableItem(
                        label = themePreference.name.lowercase().replaceFirstChar { it.titlecase() },
                        checked = themePreferenceFromAppSettings == themePreference,
                        onCheckedChange = {
                            onThemePreferenceClick(themePreference)
                        }
                    )
                }
            }
        }
    )
}

// The Knock app icon (door on the brand background)
internal val TRAY_ICON_PNG = resourcePath("/tray/tray_icon.png")
private val TRAY_ICON_ICO = resourcePath("/tray/tray_icon.ico")

/**
 * A plain file path when the resources are in the build directory (hot reload), a "jar:file:"
 * URL in a packaged app or with ./gradlew run. The tray and notification libraries accept both,
 * but KNotify fails on "file:" URLs.
 */
private fun resourcePath(path: String): String {
    val url = requireNotNull(object {}.javaClass.getResource(path)) { "Missing resource $path" }
    return if(url.protocol == "file") File(url.toURI()).absolutePath else url.toString()
}
