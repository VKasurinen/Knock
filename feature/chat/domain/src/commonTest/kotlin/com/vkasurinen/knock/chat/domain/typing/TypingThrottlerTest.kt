package com.vkasurinen.knock.chat.domain.typing

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TypingThrottlerTest {

    private val clock = FakeClock()
    private val throttler = TypingThrottler(interval = 2.seconds, clock = clock)

    @Test
    fun firstEvent_isSent() {
        assertTrue(throttler.shouldSend(CHAT_ID))
    }

    @Test
    fun eventsWithinInterval_areDropped() {
        throttler.shouldSend(CHAT_ID)

        clock.advance(1999.milliseconds)

        assertFalse(throttler.shouldSend(CHAT_ID))
    }

    @Test
    fun eventAfterInterval_isSent() {
        throttler.shouldSend(CHAT_ID)

        clock.advance(2.seconds)

        assertTrue(throttler.shouldSend(CHAT_ID))
    }

    @Test
    fun chatsAreThrottledIndependently() {
        throttler.shouldSend(CHAT_ID)

        assertTrue(throttler.shouldSend("other-chat"))
    }

    private class FakeClock: Clock {
        private var now = Instant.fromEpochMilliseconds(0)

        fun advance(duration: Duration) {
            now += duration
        }

        override fun now(): Instant = now
    }

    private companion object {
        const val CHAT_ID = "chat-1"
    }
}
