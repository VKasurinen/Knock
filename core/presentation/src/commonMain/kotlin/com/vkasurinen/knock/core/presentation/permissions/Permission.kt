package com.vkasurinen.knock.core.presentation.permissions

enum class Permission {
    NOTIFICATIONS,
    /** Only needed to save images to the gallery on Android 9 and lower */
    WRITE_STORAGE,
    RECORD_AUDIO
}