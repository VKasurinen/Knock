package com.vkasurinen.knock.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vkasurinen.knock.core.data.auth.createDataStore
import com.vkasurinen.knock.core.data.security.Encryptor
import com.vkasurinen.knock.core.data.security.KeychainEncryptor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module
actual val platformCoreDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<Encryptor> { KeychainEncryptor() }
    single<DataStore<Preferences>> {
        createDataStore(get())
    }
}