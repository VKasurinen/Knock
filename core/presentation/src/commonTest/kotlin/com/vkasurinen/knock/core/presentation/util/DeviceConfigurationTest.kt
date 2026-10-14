package com.vkasurinen.knock.core.presentation.util

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceConfigurationTest {

    @Test
    fun phonePortrait_isMobilePortrait() {
        assertEquals(DeviceConfiguration.MOBILE_PORTRAIT, configurationFor(widthDp = 411, heightDp = 891))
    }

    @Test
    fun phoneLandscape_isMobileLandscape() {
        assertEquals(DeviceConfiguration.MOBILE_LANDSCAPE, configurationFor(widthDp = 891, heightDp = 411))
        assertEquals(DeviceConfiguration.MOBILE_LANDSCAPE, configurationFor(widthDp = 732, heightDp = 412))
    }

    @Test
    fun tabletPortrait_isTabletPortrait() {
        assertEquals(DeviceConfiguration.TABLET_PORTRAIT, configurationFor(widthDp = 800, heightDp = 1280))
    }

    @Test
    fun tabletLandscape_isTabletLandscape() {
        assertEquals(DeviceConfiguration.TABLET_LANDSCAPE, configurationFor(widthDp = 1280, heightDp = 800))
    }

    @Test
    fun fullScreenDesktopWindow_isWideScreen() {
        assertEquals(DeviceConfiguration.DESKTOP, configurationFor(widthDp = 1920, heightDp = 1080))
        assertEquals(DeviceConfiguration.TABLET_LANDSCAPE, configurationFor(widthDp = 1440, heightDp = 860))
    }

    @Test
    fun wideWindow_staysWideScreen_whenMadeTaller() {
        listOf(500, 700, 899, 900, 1200, 1600).forEach { heightDp ->
            assertEquals(true, configurationFor(widthDp = 1000, heightDp = heightDp).isWideScreen, "height $heightDp")
        }
    }

    @Test
    fun narrowWindow_isNeverWideScreen() {
        listOf(300, 479, 480, 900, 1200).forEach { heightDp ->
            assertEquals(false, configurationFor(widthDp = 500, heightDp = heightDp).isWideScreen, "height $heightDp")
            assertEquals(false, configurationFor(widthDp = 839, heightDp = heightDp).isWideScreen, "height $heightDp")
        }
    }

    private fun configurationFor(widthDp: Int, heightDp: Int): DeviceConfiguration {
        val windowSizeClass = WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(
            widthDp = widthDp.toFloat(),
            heightDp = heightDp.toFloat()
        )
        return DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    }
}
