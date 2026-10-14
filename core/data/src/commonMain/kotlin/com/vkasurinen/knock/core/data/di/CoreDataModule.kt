package com.vkasurinen.knock.core.data.di

import com.vkasurinen.knock.core.data.auth.DataStoreSessionStorage
import com.vkasurinen.knock.core.data.auth.KtorAuthService
import com.vkasurinen.knock.core.data.logging.KermitLogger
import com.vkasurinen.knock.core.data.networking.HttpClientFactory
import com.vkasurinen.knock.core.data.preferences.DataStoreUserPreferences
import com.vkasurinen.knock.core.data.security.EncryptedPreferences
import com.vkasurinen.knock.core.domain.auth.AuthService
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.preferences.UserPreferences
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<KnockLogger> { KermitLogger }
    single {
        HttpClientFactory(get(), get()).create(get())
    }
    singleOf(::KtorAuthService) bind AuthService::class
    singleOf(::EncryptedPreferences)
    singleOf(::DataStoreSessionStorage) bind SessionStorage::class
    singleOf(::DataStoreUserPreferences) bind UserPreferences::class
}