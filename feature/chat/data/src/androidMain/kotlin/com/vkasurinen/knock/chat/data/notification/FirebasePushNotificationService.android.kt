package com.vkasurinen.knock.chat.data.notification

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.vkasurinen.knock.chat.domain.notification.PushNotificationService
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.coroutineContext

actual class FirebasePushNotificationService(
    private val chatNotifier: ChatNotifier,
    private val logger: KnockLogger
) : PushNotificationService {

    actual override fun observeDeviceToken(): Flow<String?> = flow {
        try {
            val fcmToken = Firebase.messaging.token.await()
            logger.info("Initial FCM token received: $fcmToken")
            emit(fcmToken)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logger.error("Failed to get FCM token", e)
            emit(null)
        }
    }

    override fun clearChatNotifications(chatId: String) {
        chatNotifier.clear(chatId)
    }
}
