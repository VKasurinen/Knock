package com.vkasurinen.knock.chat.domain.typing

import app.cash.turbine.test
import com.vkasurinen.knock.chat.domain.models.ChatEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TypingUserIdsTest {

    private val events = MutableSharedFlow<ChatEvent>()

    @Test
    fun typingEvent_addsUser() = runTest {
        events.typingUserIds(CHAT_ID).test {
            assertEquals(emptyList(), awaitItem())

            events.emit(ChatEvent.UserTyping(CHAT_ID, "alice"))

            assertEquals(listOf("alice"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun userIsRemoved_afterTimeoutWithoutNewEvents() = runTest {
        events.typingUserIds(CHAT_ID).test {
            awaitItem()
            events.emit(ChatEvent.UserTyping(CHAT_ID, "alice"))
            assertEquals(listOf("alice"), awaitItem())

            advanceTimeBy(3.seconds - 1.milliseconds)
            expectNoEvents()

            advanceTimeBy(2.milliseconds)
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun newTypingEvent_restartsTimeout() = runTest {
        events.typingUserIds(CHAT_ID).test {
            awaitItem()
            events.emit(ChatEvent.UserTyping(CHAT_ID, "alice"))
            assertEquals(listOf("alice"), awaitItem())

            advanceTimeBy(2.seconds)
            events.emit(ChatEvent.UserTyping(CHAT_ID, "alice"))

            // The original timeout would have fired at 3s
            advanceTimeBy(3.seconds - 1.milliseconds)
            expectNoEvents()

            advanceTimeBy(2.milliseconds)
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun multipleUsers_keepOrderAndExpireIndependently() = runTest {
        events.typingUserIds(CHAT_ID).test {
            awaitItem()
            events.emit(ChatEvent.UserTyping(CHAT_ID, "alice")) // t=0
            assertEquals(listOf("alice"), awaitItem())

            advanceTimeBy(1.seconds)
            events.emit(ChatEvent.UserTyping(CHAT_ID, "bob")) // t=1, expires at t=4
            assertEquals(listOf("alice", "bob"), awaitItem())

            advanceTimeBy(1.seconds)
            // t=2, Alice now expires at t=5. Typing again must not move her behind Bob
            events.emit(ChatEvent.UserTyping(CHAT_ID, "alice"))

            advanceTimeBy(2.seconds - 1.milliseconds)
            expectNoEvents()

            advanceTimeBy(2.milliseconds) // t=4.001
            assertEquals(listOf("alice"), awaitItem())

            advanceTimeBy(1.seconds - 2.milliseconds)
            expectNoEvents()

            advanceTimeBy(2.milliseconds) // t=5.001
            assertEquals(emptyList(), awaitItem())
        }
    }

    @Test
    fun eventsFromOtherChats_areIgnored() = runTest {
        events.typingUserIds(CHAT_ID).test {
            awaitItem()

            events.emit(ChatEvent.UserTyping("other-chat", "alice"))

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val CHAT_ID = "chat-1"
    }
}
