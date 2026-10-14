package com.vkasurinen.knock.chat.data.di

import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.data.attachment.DesktopImageCompressor
import com.vkasurinen.knock.chat.data.attachment.DesktopImageSaver
import com.vkasurinen.knock.chat.data.attachment.STORAGE_HTTP_CLIENT
import com.vkasurinen.knock.chat.data.audio.DesktopAudioPlayer
import com.vkasurinen.knock.chat.data.audio.DesktopAudioRecorder
import com.vkasurinen.knock.chat.data.lifecycle.AppLifecycleObserver
import com.vkasurinen.knock.chat.data.network.ConnectionErrorHandler
import com.vkasurinen.knock.chat.data.network.ConnectivityObserver
import com.vkasurinen.knock.chat.data.notification.DesktopNotifier
import com.vkasurinen.knock.chat.data.notification.FirebasePushNotificationService
import com.vkasurinen.knock.chat.database.DatabaseFactory
import com.vkasurinen.knock.chat.domain.attachment.ImageCompressor
import com.vkasurinen.knock.chat.domain.attachment.ImageSaver
import com.vkasurinen.knock.chat.domain.audio.AudioPlayer
import com.vkasurinen.knock.chat.domain.audio.AudioRecorder
import com.vkasurinen.knock.chat.domain.notification.PushNotificationService
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformChatDataModule = module {
    singleOf(::DatabaseFactory)
    singleOf(::ConnectionErrorHandler)
    singleOf(::ConnectivityObserver)
    singleOf(::AppLifecycleObserver)
    singleOf(::FirebasePushNotificationService) bind PushNotificationService::class
    singleOf(::DesktopNotifier)

    singleOf(::AttachmentFileStorage)
    singleOf(::DesktopImageCompressor) bind ImageCompressor::class
    singleOf(::DesktopAudioRecorder) bind AudioRecorder::class
    single<AudioPlayer> {
        DesktopAudioPlayer(
            storageHttpClient = get(named(STORAGE_HTTP_CLIENT)),
            applicationScope = get(),
            logger = get()
        )
    }
    single<ImageSaver> {
        DesktopImageSaver(
            fileStorage = get(),
            storageHttpClient = get(named(STORAGE_HTTP_CLIENT)),
            logger = get()
        )
    }
}
