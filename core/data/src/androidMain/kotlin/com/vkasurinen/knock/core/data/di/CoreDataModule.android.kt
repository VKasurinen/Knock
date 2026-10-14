package com.vkasurinen.knock.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vkasurinen.knock.core.data.auth.createDataStore
import com.vkasurinen.knock.core.data.security.Encryptor
import com.vkasurinen.knock.core.data.security.KeystoreEncryptor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<Encryptor> { KeystoreEncryptor() }
    single<DataStore<Preferences>> {
        createDataStore(androidContext(), get())
    }
}