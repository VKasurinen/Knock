package com.vkasurinen.knock

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.window.application
import com.vkasurinen.knock.deeplink.DesktopDeepLinkHandler
import com.vkasurinen.knock.di.desktopModule
import com.vkasurinen.knock.di.initKoin
import com.vkasurinen.knock.theme.rememberAppTheme
import com.vkasurinen.knock.windows.KnockWindow
import io.github.kdroidfilter.knotify.builder.AppConfig
import io.github.kdroidfilter.knotify.builder.NotificationInitializer
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

fun main(args: Array<String>) {
    initKoin {
        modules(desktopModule)
    }
    NotificationInitializer.configure(
        AppConfig(
            appName = "Knock",
            smallIcon = TRAY_ICON_PNG
        )
    )

    val applicationStateHolder = KoinPlatform.getKoin().get<ApplicationStateHolder>()
    DesktopDeepLinkHandler.setup(onDeepLink = applicationStateHolder::onDeepLink)
    DesktopDeepLinkHandler.findDeepLink(args)?.let(applicationStateHolder::onDeepLink)

    application {
        val applicationStateHolder = koinInject<ApplicationStateHolder>()
        val applicationState by applicationStateHolder.state.collectAsState()
        val windows = applicationState.windows

        LaunchedEffect(windows) {
            if(windows.isEmpty()) {
                exitApplication()
            }
        }

        val appTheme = rememberAppTheme(applicationState.themePreference)

        for(window in windows) {
            key(window.id) {
                KnockWindow(
                    appTheme = appTheme,
                    deepLinkRequest = applicationState.pendingDeepLink?.takeIf { it.windowId == window.id },
                    onDeepLinkHandled = applicationStateHolder::onDeepLinkHandled,
                    onCloseRequest = {
                        applicationStateHolder.onWindowCloseRequest(window.id)
                    },
                    onAddWindowClick = applicationStateHolder::onAddWindowClick,
                    onFocusChanged = { isFocused ->
                        applicationStateHolder.onWindowFocusChanged(window.id, isFocused)
                    }
                )
            }
        }

        KnockTrayMenu(
            themePreferenceFromAppSettings = applicationState.themePreference,
            onThemePreferenceClick = applicationStateHolder::onThemePreferenceClick
        )
    }
}
