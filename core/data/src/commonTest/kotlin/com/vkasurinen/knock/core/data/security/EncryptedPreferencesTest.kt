package com.vkasurinen.knock.core.data.security

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vkasurinen.knock.core.data.fakes.FakeEncryptor
import com.vkasurinen.knock.core.data.fakes.FakeLogger
import com.vkasurinen.knock.core.data.fakes.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EncryptedPreferencesTest {

    private lateinit var dataStore: FakePreferencesDataStore
    private lateinit var encryptor: FakeEncryptor
    private lateinit var preferences: EncryptedPreferences

    @BeforeTest
    fun setUp() {
        dataStore = FakePreferencesDataStore()
        encryptor = FakeEncryptor()
        preferences = EncryptedPreferences(dataStore, encryptor, FakeLogger)
    }

    @Test
    fun putString_storesCiphertextInsteadOfPlaintext() = runTest {
        val value = """{"accessToken":"secret-token"}"""

        preferences.putString(KEY, value)

        val stored = dataStore.current[stringPreferencesKey(KEY)]
        assertNotNull(stored)
        assertFalse(stored.contains("secret-token"))
        assertEquals(value, encryptor.decryptFromBase64(stored))
    }

    @Test
    fun observeString_returnsDecryptedValue() = runTest {
        preferences.putString(KEY, "hello")

        assertEquals("hello", preferences.observeString(KEY).first())
    }

    @Test
    fun putString_withNull_removesKey() = runTest {
        preferences.putString(KEY, "hello")

        preferences.putString(KEY, null)

        assertNull(dataStore.current[stringPreferencesKey(KEY)])
        assertNull(preferences.observeString(KEY).first())
    }

    @Test
    fun observeString_whenDecryptionFails_returnsNull() = runTest {
        preferences.putString(KEY, "hello")
        // A new instance has nothing cached, like after an app restart
        val restartedPreferences = EncryptedPreferences(dataStore, encryptor, FakeLogger)
        encryptor.failOnDecrypt = true

        assertNull(restartedPreferences.observeString(KEY).first())
    }

    @Test
    fun observeString_decryptsUnchangedValueOnlyOnce() = runTest {
        preferences.putString(KEY, "hello")
        val restartedPreferences = EncryptedPreferences(dataStore, encryptor, FakeLogger)

        repeat(3) {
            assertEquals("hello", restartedPreferences.observeString(KEY).first())
        }

        assertEquals(1, encryptor.decryptCount)
    }

    @Test
    fun observeString_afterWrite_doesNotDecrypt() = runTest {
        preferences.putString(KEY, "hello")

        assertEquals("hello", preferences.observeString(KEY).first())
        assertEquals(0, encryptor.decryptCount)
    }

    @Test
    fun migration_encryptsPlaintextJsonAndKeepsOtherKeys() = runTest {
        val plaintext = """{"accessToken":"secret-token"}"""
        val otherKey = stringPreferencesKey("OTHER")
        val initial = preferencesOf(
            stringPreferencesKey(KEY) to plaintext,
            otherKey to "untouched"
        )
        val migration = EncryptPlaintextPreferencesMigration(encryptor, listOf(KEY))

        assertTrue(migration.shouldMigrate(initial))
        val migrated = migration.migrate(initial)

        val stored = migrated[stringPreferencesKey(KEY)]
        assertNotNull(stored)
        assertFalse(stored.contains("secret-token"))
        assertEquals(plaintext, encryptor.decryptFromBase64(stored))
        assertEquals("untouched", migrated[otherKey])
    }

    @Test
    fun migration_skipsAlreadyEncryptedValues() = runTest {
        val encrypted = encryptor.encryptToBase64("""{"accessToken":"secret-token"}""")
        val initial = preferencesOf(stringPreferencesKey(KEY) to encrypted)
        val migration = EncryptPlaintextPreferencesMigration(encryptor, listOf(KEY))

        assertFalse(migration.shouldMigrate(initial))
    }

    private companion object {
        const val KEY = "KEY_TEST"
    }
}
