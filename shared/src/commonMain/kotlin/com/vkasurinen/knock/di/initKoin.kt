package com.vkasurinen.knock.di

import com.vkasurinen.knock.auth.presentation.di.authPresentationModule
import com.vkasurinen.knock.chat.data.di.chatDataModule
import com.vkasurinen.knock.chat.presentation.di.chatPresentationModule
import com.vkasurinen.knock.core.data.di.coreDataModule
import com.vkasurinen.knock.core.presentation.di.corePresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            appModule,
            chatPresentationModule,
            corePresentationModule,
            chatDataModule
        )
    }
}