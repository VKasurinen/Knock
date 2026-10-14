package com.vkasurinen.knock.core.domain.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    fun observeTypingIndicatorsEnabled(): Flow<Boolean>
    suspend fun setTypingIndicatorsEnabled(enabled: Boolean)

    fun observeAppLanguage(): Flow<AppLanguage>
    suspend fun setAppLanguage(language: AppLanguage)
}
