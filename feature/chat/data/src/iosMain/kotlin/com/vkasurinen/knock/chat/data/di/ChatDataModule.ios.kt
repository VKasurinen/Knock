package com.vkasurinen.knock.chat.data.di


import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.data.attachment.IosImageCompressor
import com.vkasurinen.knock.chat.data.attachment.IosImageSaver
import com.vkasurinen.knock.chat.data.attachment.STORAGE_HTTP_CLIENT
import com.vkasurinen.knock.chat.data.audio.IosAudioPlayer
import com.vkasurinen.knock.chat.data.audio.IosAudioRecorder
import com.vkasurinen.knock.chat.data.lifecycle.AppLifecycleObserver
import com.vkasurinen.knock.chat.data.network.ConnectionErrorHandler
import com.vkasurinen.knock.chat.data.network.ConnectivityObserver
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
    single { DatabaseFactory() }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
    singleOf(::FirebasePushNotificationService) bind PushNotificationService::class

    single { AttachmentFileStorage() }
    singleOf(::IosImageCompressor) bind ImageCompressor::class
    single<AudioRecorder> { IosAudioRecorder(get(), get()) }
    single<AudioPlayer> { IosAudioPlayer(get()) }
    single<ImageSaver> {
        IosImageSaver(
            fileStorage = get(),
            storageHttpClient = get(named(STORAGE_HTTP_CLIENT)),
            logger = get()
        )
    }
}