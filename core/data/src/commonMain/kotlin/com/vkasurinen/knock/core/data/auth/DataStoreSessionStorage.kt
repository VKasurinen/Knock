package com.vkasurinen.knock.core.data.auth

import com.vkasurinen.knock.core.data.dto.AuthInfoSerializable
import com.vkasurinen.knock.core.data.mappers.toDomain
import com.vkasurinen.knock.core.data.mappers.toSerializable
import com.vkasurinen.knock.core.data.security.EncryptedPreferences
import com.vkasurinen.knock.core.domain.auth.AuthInfo
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val preferences: EncryptedPreferences
): SessionStorage {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return preferences.observeString(KEY_AUTH_INFO).map { serializedJson ->
            serializedJson?.let {
                json.decodeFromString<AuthInfoSerializable>(it).toDomain()
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        preferences.putString(
            key = KEY_AUTH_INFO,
            value = info?.let { json.encodeToString(it.toSerializable()) }
        )
    }

    companion object {
        const val KEY_AUTH_INFO = "KEY_AUTH_INFO"
    }
}
