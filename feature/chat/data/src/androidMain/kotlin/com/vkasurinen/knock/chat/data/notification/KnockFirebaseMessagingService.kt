package com.vkasurinen.knock.chat.data.notification

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vkasurinen.knock.chat.database.KnockChatDatabase
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.notification.DeviceTokenService
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class KnockFirebaseMessagingService: FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val applicationScope by inject<CoroutineScope>()
    private val chatRepository by inject<ChatRepository>()
    private val chatNotifier by inject<ChatNotifier>()
    private val database by inject<KnockChatDatabase>()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        applicationScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().first()
            if(authInfo != null) {
                deviceTokenService.registerToken(
                    token = token,
                    platform = "ANDROID"
                )
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        if(data["type"] != "new_message") {
            return
        }
        val chatId = data["chatId"] ?: return

        applicationScope.launch {
            // The message reached this device, which the sender sees as two ticks
            chatRepository.markChatDelivered(chatId)

            // While the app is open the chat itself shows new messages
            if(isAppInForeground()) {
                return@launch
            }

            val isGroup = data["isGroup"].toBoolean()
            chatNotifier.showMessage(
                chatId = chatId,
                message = ChatNotifier.NotificationMessage(
                    messageId = data["messageId"],
                    senderName = data["senderName"].orEmpty(),
                    body = data["body"].orEmpty(),
                    sentAt = data["sentAt"]?.toLongOrNull() ?: message.sentTime
                ),
                chatTitle = data["chatName"] ?: if(isGroup) groupTitle(chatId) else null,
                isGroup = isGroup
            )
        }
    }

    private fun isAppInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    /** An unnamed group is titled with its members' names, like in the chat list */
    private suspend fun groupTitle(chatId: String): String? {
        val localUserId = sessionStorage.observeAuthInfo().first()?.user?.id
        return database.chatDao.getChatById(chatId)
            ?.participants
            ?.filter { it.userId != localUserId }
            ?.joinToString { it.username }
            ?.takeIf { it.isNotBlank() }
    }
}
