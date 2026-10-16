package com.vkasurinen.knock.chat.data.di


import com.vkasurinen.knock.chat.data.attachment.AndroidImageCompressor
import com.vkasurinen.knock.chat.data.attachment.AndroidImageSaver
import com.vkasurinen.knock.chat.data.attachment.AttachmentFileStorage
import com.vkasurinen.knock.chat.data.attachment.STORAGE_HTTP_CLIENT
import com.vkasurinen.knock.chat.data.audio.AndroidAudioPlayer
import com.vkasurinen.knock.chat.data.audio.AndroidAudioRecorder
import com.vkasurinen.knock.chat.data.lifecycle.AppLifecycleObserver
import com.vkasurinen.knock.chat.data.network.ConnectionErrorHandler
import com.vkasurinen.knock.chat.data.network.ConnectivityObserver
import com.vkasurinen.knock.chat.data.notification.ChatNotifier
import com.vkasurinen.knock.chat.data.notification.FirebasePushNotificationService
import com.vkasurinen.knock.chat.database.DatabaseFactory
import com.vkasurinen.knock.chat.domain.attachment.ImageCompressor
import com.vkasurinen.knock.chat.domain.attachment.ImageSaver
import com.vkasurinen.knock.chat.domain.audio.AudioPlayer
import com.vkasurinen.knock.chat.domain.audio.AudioRecorder
import com.vkasurinen.knock.chat.domain.notification.PushNotificationService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory(androidContext()) }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)

    single { ChatNotifier(androidContext()) }
    singleOf(::FirebasePushNotificationService) bind PushNotificationService::class

    single { AttachmentFileStorage(androidContext()) }
    singleOf(::AndroidImageCompressor) bind ImageCompressor::class
    single<AudioRecorder> { AndroidAudioRecorder(androidContext(), get(), get()) }
    single<AudioPlayer> { AndroidAudioPlayer(androidContext(), get(), get()) }
    single<ImageSaver> {
        AndroidImageSaver(
            context = androidContext(),
            fileStorage = get(),
            storageHttpClient = get(named(STORAGE_HTTP_CLIENT)),
            logger = get()
        )
    }
}