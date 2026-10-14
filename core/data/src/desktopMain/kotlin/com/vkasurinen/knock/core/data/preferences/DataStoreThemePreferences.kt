package com.vkasurinen.knock.core.data.preferences

import com.vkasurinen.knock.core.data.security.EncryptedPreferences
import com.vkasurinen.knock.core.domain.preferences.ThemePreference
import com.vkasurinen.knock.core.domain.preferences.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreThemePreferences(
    private val preferences: EncryptedPreferences
): ThemePreferences {

    override fun observeThemePreference(): Flow<ThemePreference> {
        return preferences.observeString(KEY_THEME_PREFERENCE).map { value ->
            ThemePreference.entries.find { it.name == value } ?: ThemePreference.SYSTEM
        }
    }

    override suspend fun updateThemePreference(theme: ThemePreference) {
        preferences.putString(KEY_THEME_PREFERENCE, theme.name)
    }

    private companion object {
        const val KEY_THEME_PREFERENCE = "KEY_THEME_PREFERENCE"
    }
}
