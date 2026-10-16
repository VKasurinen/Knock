package com.vkasurinen.knock.chat.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Forgets a chat's notification messages when the user swipes the notification away, so the next
 * notification starts fresh.
 */
class ChatNotificationDismissedReceiver: BroadcastReceiver(), KoinComponent {

    private val chatNotifier by inject<ChatNotifier>()

    override fun onReceive(context: Context, intent: Intent) {
        val chatId = intent.getStringExtra(ChatNotifier.EXTRA_CHAT_ID) ?: return
        chatNotifier.forgetHistory(chatId)
    }
}
