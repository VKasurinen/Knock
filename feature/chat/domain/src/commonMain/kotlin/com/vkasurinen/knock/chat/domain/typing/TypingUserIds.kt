package com.vkasurinen.knock.chat.domain.typing

import com.vkasurinen.knock.chat.domain.models.ChatEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val TYPING_TIMEOUT = 3.seconds

/**
 * The server only tells us when someone types, never when they stop. A user therefore counts
 * as typing until [timeout] has passed without a new typing event from them.
 */
fun Flow<ChatEvent>.typingUserIds(
    chatId: String,
    timeout: Duration = TYPING_TIMEOUT
): Flow<List<String>> = channelFlow {
    // Insertion ordered, so names are shown in the order people started typing
    val expiryJobs = LinkedHashMap<String, Job>()
    val mutex = Mutex()

    send(emptyList())

    filterIsInstance<ChatEvent.UserTyping>()
        .filter { it.chatId == chatId }
        .collect { event ->
            val typingUserIds = mutex.withLock {
                expiryJobs[event.userId]?.cancel()
                expiryJobs[event.userId] = launch {
                    delay(timeout)
                    val remainingUserIds = mutex.withLock {
                        expiryJobs.remove(event.userId)
                        expiryJobs.keys.toList()
                    }
                    send(remainingUserIds)
                }
                expiryJobs.keys.toList()
            }
            send(typingUserIds)
        }
}.distinctUntilChanged()
