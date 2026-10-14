package com.vkasurinen.knock.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.vkasurinen.knock.core.data.auth.createDataStore
import com.vkasurinen.knock.core.data.preferences.DataStoreThemePreferences
import com.vkasurinen.knock.core.data.security.DesktopEncryptor
import com.vkasurinen.knock.core.data.security.Encryptor
import com.vkasurinen.knock.core.domain.preferences.ThemePreferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<Encryptor> { DesktopEncryptor() }
    single<DataStore<Preferences>> {
        createDataStore(get())
    }
    singleOf(::DataStoreThemePreferences) bind ThemePreferences::class
}
