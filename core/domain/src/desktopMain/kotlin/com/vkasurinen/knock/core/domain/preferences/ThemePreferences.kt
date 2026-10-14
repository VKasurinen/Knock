package com.vkasurinen.knock.core.domain.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Light, dark or the OS theme. Only desktop lets the user choose, mobile follows the system.
 */
interface ThemePreferences {
    fun observeThemePreference(): Flow<ThemePreference>
    suspend fun updateThemePreference(theme: ThemePreference)
}
