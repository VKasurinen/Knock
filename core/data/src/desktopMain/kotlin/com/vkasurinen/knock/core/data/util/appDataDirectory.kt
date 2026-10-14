package com.vkasurinen.knock.core.data.util

import java.io.File

val appDataDirectory: File
    get() {
        val userHome = System.getProperty("user.home")
        return when(currentOs) {
            DesktopOs.WINDOWS -> File(System.getenv("APPDATA"), "Knock")
            DesktopOs.MACOS -> File(userHome, "Library/Application Support/Knock")
            DesktopOs.LINUX -> File(userHome, ".local/share/Knock")
        }
    }