package com.vkasurinen.knock.core.data.preferences

import com.vkasurinen.knock.core.data.fakes.FakeEncryptor
import com.vkasurinen.knock.core.data.fakes.FakeLogger
import com.vkasurinen.knock.core.data.fakes.FakePreferencesDataStore
import com.vkasurinen.knock.core.data.security.EncryptedPreferences
import com.vkasurinen.knock.core.domain.preferences.ThemePreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DataStoreThemePreferencesTest {

    private lateinit var encryptedPreferences: EncryptedPreferences
    private lateinit var themePreferences: DataStoreThemePreferences

    @BeforeTest
    fun setUp() {
        encryptedPreferences = EncryptedPreferences(FakePreferencesDataStore(), FakeEncryptor(), FakeLogger)
        themePreferences = DataStoreThemePreferences(encryptedPreferences)
    }

    @Test
    fun nothingStored_followsSystem() = runTest {
        assertEquals(ThemePreference.SYSTEM, themePreferences.observeThemePreference().first())
    }

    @Test
    fun updatedPreference_isObserved() = runTest {
        themePreferences.updateThemePreference(ThemePreference.DARK)

        assertEquals(ThemePreference.DARK, themePreferences.observeThemePreference().first())
    }

    @Test
    fun unknownStoredValue_followsSystem() = runTest {
        encryptedPreferences.putString("KEY_THEME_PREFERENCE", "PURPLE")

        assertEquals(ThemePreference.SYSTEM, themePreferences.observeThemePreference().first())
    }
}
