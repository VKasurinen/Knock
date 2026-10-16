package com.vkasurinen.knock.chat.domain.notification

import kotlinx.coroutines.flow.Flow

interface PushNotificationService {
    fun observeDeviceToken(): Flow<String?>

    /**
     * Removes the notifications of [chatId] from the system tray, e.g. when the chat is opened.
     * A no-op on platforms that don't show push notifications per chat.
     */
    fun clearChatNotifications(chatId: String) = Unit
}
