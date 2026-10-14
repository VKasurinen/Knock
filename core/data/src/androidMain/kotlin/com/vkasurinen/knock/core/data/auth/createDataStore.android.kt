package com.vkasurinen.knock.core.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vkasurinen.knock.core.data.security.Encryptor

fun createDataStore(
    context: Context,
    encryptor: Encryptor
): DataStore<Preferences> {
    return createDataStore(encryptor) {
        context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}