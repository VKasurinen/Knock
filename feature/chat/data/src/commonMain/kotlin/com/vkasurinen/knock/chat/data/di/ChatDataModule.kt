package com.vkasurinen.knock.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vkasurinen.knock.chat.data.attachment.KtorChatAttachmentService
import com.vkasurinen.knock.chat.data.attachment.STORAGE_HTTP_CLIENT
import com.vkasurinen.knock.chat.data.chat.KtorChatService
import com.vkasurinen.knock.chat.data.chat.OfflineFirstChatRepository
import com.vkasurinen.knock.chat.data.chat.WebSocketChatConnectionClient
import com.vkasurinen.knock.chat.data.message.KtorChatMessageService
import com.vkasurinen.knock.chat.data.message.OfflineFirstMessageRepository
import com.vkasurinen.knock.chat.data.network.ConnectionRetryHandler
import com.vkasurinen.knock.chat.data.network.KtorWebSocketConnector
import com.vkasurinen.knock.chat.data.notification.KtorDeviceTokenService
import com.vkasurinen.knock.chat.data.participant.KtorChatParticipantService
import com.vkasurinen.knock.chat.data.participant.OfflineFirstChatParticipantRepository
import com.vkasurinen.knock.chat.data.typing.WebSocketTypingRepository
import com.vkasurinen.knock.chat.database.DatabaseFactory
import com.vkasurinen.knock.chat.domain.attachment.ChatAttachmentService
import com.vkasurinen.knock.chat.domain.chat.ChatConnectionClient
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.chat.ChatService
import com.vkasurinen.knock.chat.domain.message.ChatMessageService
import com.vkasurinen.knock.chat.domain.message.MessageRepository
import com.vkasurinen.knock.chat.domain.notification.DeviceTokenService
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantRepository
import com.vkasurinen.knock.chat.domain.participant.ChatParticipantService
import com.vkasurinen.knock.chat.domain.typing.TypingRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module

val chatDataModule = module {
    includes(platformChatDataModule)

    singleOf(::KtorChatParticipantService) bind ChatParticipantService::class
    singleOf(::KtorChatService) bind ChatService::class
    singleOf(::OfflineFirstChatRepository) bind ChatRepository::class
    singleOf(::OfflineFirstMessageRepository) bind MessageRepository::class
    singleOf(::WebSocketChatConnectionClient) bind ChatConnectionClient::class
    singleOf(::ConnectionRetryHandler)
    singleOf(::KtorWebSocketConnector)
    singleOf(::KtorChatMessageService) bind ChatMessageService::class
    singleOf(::KtorDeviceTokenService) bind DeviceTokenService::class
    singleOf(::OfflineFirstChatParticipantRepository) bind ChatParticipantRepository::class
    singleOf(::WebSocketTypingRepository) bind TypingRepository::class
    single(named(STORAGE_HTTP_CLIENT)) {
        // Plain client for the storage provider's URLs: no API key, JWT or body logging
        HttpClient(get<HttpClientEngine>()) {
            install(HttpTimeout) {
                requestTimeoutMillis = 2 * 60_000L
                socketTimeoutMillis = 60_000L
            }
        }
    }
    single<ChatAttachmentService> {
        KtorChatAttachmentService(
            httpClient = get(),
            uploadHttpClient = get(named(STORAGE_HTTP_CLIENT))
        )
    }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            // The database is only a cache of the server state, so it can be rebuilt on schema changes
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}