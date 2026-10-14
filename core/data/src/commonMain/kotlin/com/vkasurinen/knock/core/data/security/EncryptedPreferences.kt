package com.vkasurinen.knock.core.data.security

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import kotlin.io.encoding.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thin wrapper around a [DataStore] that stores every value as Base64 encoded ciphertext.
 */
class EncryptedPreferences(
    private val dataStore: DataStore<Preferences>,
    private val encryptor: Encryptor,
    private val logger: KnockLogger
) {

    // Decrypting goes through the platform keystore, which is slow on real devices. Values are
    // read far more often than they change, so the last decrypted value per key is kept and
    // reused for as long as the stored ciphertext stays the same.
    private val cacheMutex = Mutex()
    private val decryptedCache = mutableMapOf<String, CachedValue>()

    fun observeString(key: String): Flow<String?> {
        val preferencesKey = stringPreferencesKey(key)
        return dataStore.data
            .map { preferences ->
                preferences[preferencesKey]?.let { decryptCached(key, it) }
            }
            .distinctUntilChanged()
    }

    suspend fun putString(key: String, value: String?) {
        val preferencesKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            if(value == null) {
                preferences.remove(preferencesKey)
            } else {
                val encrypted = encryptor.encryptToBase64(value)
                preferences[preferencesKey] = encrypted
                cacheMutex.withLock {
                    decryptedCache[key] = CachedValue(encrypted = encrypted, decrypted = value)
                }
            }
        }
    }

    private suspend fun decryptCached(key: String, encrypted: String): String? {
        cacheMutex.withLock {
            decryptedCache[key]
                ?.takeIf { it.encrypted == encrypted }
                ?.let { return it.decrypted }
        }

        val decrypted = decryptOrNull(key, encrypted) ?: return null
        cacheMutex.withLock {
            decryptedCache[key] = CachedValue(encrypted = encrypted, decrypted = decrypted)
        }
        return decrypted
    }

    private fun decryptOrNull(key: String, encrypted: String): String? {
        return try {
            encryptor.decryptFromBase64(encrypted)
        } catch(e: Exception) {
            // E.g. the platform key was lost after a reinstall or backup restore.
            // Treat the value as missing, the user will simply need to log in again.
            logger.error("Failed to decrypt preference $key", e)
            null
        }
    }
}

private class CachedValue(
    val encrypted: String,
    val decrypted: String
)

internal fun Encryptor.encryptToBase64(value: String): String {
    return Base64.encode(encrypt(value.encodeToByteArray()))
}

internal fun Encryptor.decryptFromBase64(value: String): String {
    return decrypt(Base64.decode(value)).decodeToString()
}

/**
 * Encrypts values that were stored as plaintext by older app versions.
 * Base64 never contains '{', so plaintext JSON is easy to tell apart from ciphertext.
 */
class EncryptPlaintextPreferencesMigration(
    private val encryptor: Encryptor,
    private val keys: List<String>
): DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return keys.any { key -> currentData[stringPreferencesKey(key)].isPlaintextJson() }
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val preferences = currentData.toMutablePreferences()
        keys.forEach { key ->
            val preferencesKey = stringPreferencesKey(key)
            val value = preferences[preferencesKey]
            if(value.isPlaintextJson()) {
                preferences[preferencesKey] = encryptor.encryptToBase64(value!!)
            }
        }
        return preferences.toPreferences()
    }

    override suspend fun cleanUp() = Unit

    private fun String?.isPlaintextJson(): Boolean {
        return this?.trimStart()?.startsWith("{") == true
    }
}
