package com.vkasurinen.knock.core.data.auth

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.vkasurinen.knock.core.data.security.EncryptPlaintextPreferencesMigration
import com.vkasurinen.knock.core.data.security.Encryptor
import okio.Path.Companion.toPath

fun createDataStore(
    encryptor: Encryptor,
    producePath: () -> String
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        migrations = listOf<DataMigration<Preferences>>(
            EncryptPlaintextPreferencesMigration(
                encryptor = encryptor,
                keys = listOf(DataStoreSessionStorage.KEY_AUTH_INFO)
            )
        )
    ) {
        producePath().toPath()
    }
}

internal const val DATA_STORE_FILE_NAME = "prefs.preferences_pb"
