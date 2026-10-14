package com.vkasurinen.knock

import com.vkasurinen.knock.core.domain.preferences.AppLanguage

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM
)
