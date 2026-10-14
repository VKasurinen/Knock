package com.vkasurinen.knock.chat.domain.typing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Limits outgoing typing events to one per [interval] per chat. The receivers keep
 * a user as typing for [TYPING_TIMEOUT], so the interval needs to be shorter than that.
 */
class TypingThrottler(
    private val interval: Duration = 2.seconds,
    private val clock: Clock = Clock.System
) {
    private val lastSentAt = mutableMapOf<String, Instant>()

    fun shouldSend(chatId: String): Boolean {
        val now = clock.now()
        val last = lastSentAt[chatId]
        if(last != null && now - last < interval) {
            return false
        }
        lastSentAt[chatId] = now
        return true
    }
}
