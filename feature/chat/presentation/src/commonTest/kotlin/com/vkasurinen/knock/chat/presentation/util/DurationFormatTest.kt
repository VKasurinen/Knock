package com.vkasurinen.knock.chat.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationFormatTest {

    @Test
    fun secondsArePadded() {
        assertEquals("0:05", formatDuration(5_000))
    }

    @Test
    fun minutesAreNotPadded() {
        assertEquals("10:00", formatDuration(600_000))
    }

    @Test
    fun durationIsRoundedToNearestSecond() {
        assertEquals("0:02", formatDuration(1_600))
        assertEquals("0:01", formatDuration(1_400))
    }

    @Test
    fun negativeDuration_isZero() {
        assertEquals("0:00", formatDuration(-500))
    }
}
