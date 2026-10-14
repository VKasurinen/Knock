package com.vkasurinen.knock.core.data.preferences

import com.vkasurinen.knock.core.data.security.EncryptedPreferences
import com.vkasurinen.knock.core.domain.preferences.AppLanguage
import com.vkasurinen.knock.core.domain.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreUserPreferences(
    private val preferences: EncryptedPreferences
): UserPreferences {

    override fun observeTypingIndicatorsEnabled(): Flow<Boolean> {
        return preferences.observeString(KEY_TYPING_INDICATORS_ENABLED).map { value ->
            value?.toBooleanStrictOrNull() ?: true
        }
    }

    override suspend fun setTypingIndicatorsEnabled(enabled: Boolean) {
        preferences.putString(KEY_TYPING_INDICATORS_ENABLED, enabled.toString())
    }

    override fun observeAppLanguage(): Flow<AppLanguage> {
        return preferences.observeString(KEY_APP_LANGUAGE).map { value ->
            AppLanguage.entries.find { it.name == value } ?: AppLanguage.SYSTEM
        }
    }

    override suspend fun setAppLanguage(language: AppLanguage) {
        preferences.putString(KEY_APP_LANGUAGE, language.name)
    }

    private companion object {
        const val KEY_TYPING_INDICATORS_ENABLED = "KEY_TYPING_INDICATORS_ENABLED"
        const val KEY_APP_LANGUAGE = "KEY_APP_LANGUAGE"
    }
}
