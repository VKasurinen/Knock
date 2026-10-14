package com.vkasurinen.knock.core.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

@Composable
fun currentDeviceConfiguration(): DeviceConfiguration {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
}

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP;

    val isMobile: Boolean
        get() = this in listOf(MOBILE_PORTRAIT, MOBILE_LANDSCAPE)

    val isWideScreen: Boolean
        get() = this in listOf(TABLET_LANDSCAPE, DESKTOP)

    companion object {
        /**
         * The window size class only tells which bucket the window is in, not its exact size:
         * width 0 / 600 / 840 dp and height 0 / 480 / 900 dp.
         *
         * Width decides the layout, so making a window taller never changes it. Height only
         * separates phones in landscape (wide but short) from tablets and desktop windows.
         */
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            return with(windowSizeClass) {
                val isWidthExpanded = isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
                val isWidthMedium = isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
                val isHeightCompact = !isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
                val isHeightExpanded = isHeightAtLeastBreakpoint(HEIGHT_DP_EXPANDED_LOWER_BOUND)

                when {
                    !isWidthMedium -> MOBILE_PORTRAIT
                    isHeightCompact -> MOBILE_LANDSCAPE
                    !isWidthExpanded -> TABLET_PORTRAIT
                    !isHeightExpanded -> TABLET_LANDSCAPE
                    else -> DESKTOP
                }
            }
        }
    }
}
