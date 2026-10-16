package com.vkasurinen.knock.chat.data.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

/**
 * Shows one notification per chat with its latest messages, like messaging apps do. The backend
 * sends data-only pushes, so the app builds the notification and can add to the existing one.
 */
class ChatNotifier(
    private val context: Context
) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class NotificationMessage(
        val messageId: String?,
        val senderName: String,
        val body: String,
        val sentAt: Long
    )

    @SuppressLint("MissingPermission") // Checked with areNotificationsEnabled
    @Synchronized
    fun showMessage(
        chatId: String,
        message: NotificationMessage,
        chatTitle: String?,
        isGroup: Boolean
    ) {
        if(!notificationManager.areNotificationsEnabled()) {
            return
        }
        ensureChannel()

        val history = (loadHistory(chatId) + message)
            .distinctBy { it.messageId ?: "${it.sentAt}${it.body}" }
            .sortedBy { it.sentAt }
            .takeLast(MAX_MESSAGES)
        saveHistory(chatId, history)

        val style = NotificationCompat.MessagingStyle(
            Person.Builder().setName(texts.you).build()
        )
            .setConversationTitle(if(isGroup) chatTitle ?: texts.groupChat else null)
            .setGroupConversation(isGroup)
        history.forEach { item ->
            style.addMessage(
                item.body,
                item.sentAt,
                Person.Builder().setName(item.senderName).build()
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon())
            .setColor(BRAND_COLOR)
            .setStyle(style)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setWhen(message.sentAt)
            .setNumber(history.size)
            .setContentIntent(openChatIntent(chatId))
            .setDeleteIntent(dismissIntent(chatId))
            .build()

        notificationManager.notify(chatId, NOTIFICATION_ID, notification)
    }

    /** Removes the chat's notification and forgets its messages, e.g. when the chat is opened */
    @Synchronized
    fun clear(chatId: String) {
        notificationManager.cancel(chatId, NOTIFICATION_ID)
        forgetHistory(chatId)
    }

    @Synchronized
    fun forgetHistory(chatId: String) {
        preferences.edit().remove(chatId).apply()
    }

    private fun loadHistory(chatId: String): List<NotificationMessage> {
        val stored = preferences.getString(chatId, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<NotificationMessage>>(stored) }.getOrDefault(emptyList())
    }

    private fun saveHistory(chatId: String, history: List<NotificationMessage>) {
        preferences.edit().putString(chatId, json.encodeToString(history)).apply()
    }

    private fun openChatIntent(chatId: String): PendingIntent? {
        // MainActivity opens the chat from this extra, the same way as before
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra(EXTRA_CHAT_ID, chatId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            ?: return null
        return PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun dismissIntent(chatId: String): PendingIntent {
        val intent = Intent(context, ChatNotificationDismissedReceiver::class.java)
            .putExtra(EXTRA_CHAT_ID, chatId)
        return PendingIntent.getBroadcast(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if(manager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, texts.channelName, NotificationManager.IMPORTANCE_HIGH)
        )
    }

    @SuppressLint("DiscouragedApi") // The icon lives in the app module
    private fun smallIcon(): Int {
        val icon = context.resources.getIdentifier("ic_launcher_knock_monochrome", "drawable", context.packageName)
        return if(icon != 0) icon else context.applicationInfo.icon
    }

    private class Texts(val you: String, val groupChat: String, val channelName: String)

    // This module has no Android string resources; the app supports English and Finnish
    private val texts: Texts
        get() = if(Locale.getDefault().language == "fi") {
            Texts(you = "Sinä", groupChat = "Ryhmächat", channelName = "Viestit")
        } else {
            Texts(you = "You", groupChat = "Group chat", channelName = "Messages")
        }

    companion object {
        const val EXTRA_CHAT_ID = "chatId"
        private const val CHANNEL_ID = "messages"
        private const val NOTIFICATION_ID = 0
        private const val MAX_MESSAGES = 7
        private const val PREFERENCES_NAME = "chat_notifications"
        private const val BRAND_COLOR = 0xFF6B4630.toInt()
    }
}
