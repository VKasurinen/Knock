package com.vkasurinen.knock.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vkasurinen.knock.core.data.security.Encryptor
import com.vkasurinen.knock.core.data.util.appDataDirectory
import java.io.File

fun createDataStore(encryptor: Encryptor): DataStore<Preferences> = createDataStore(encryptor) {
    val directory = appDataDirectory

    if(!directory.exists()) {
        directory.mkdirs()
    }

    File(directory, DATA_STORE_FILE_NAME).absolutePath
}
