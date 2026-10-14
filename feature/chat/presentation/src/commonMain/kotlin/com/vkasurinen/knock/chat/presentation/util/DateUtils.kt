package com.vkasurinen.knock.chat.presentation.util

import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.today
import knock.feature.chat.presentation.generated.resources.today_x
import knock.feature.chat.presentation.generated.resources.yesterday
import knock.feature.chat.presentation.generated.resources.yesterday_x
import com.vkasurinen.knock.core.presentation.util.UiText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

object DateUtils {

    // Building a format is expensive, so they are created once instead of per message
    private val timeFormat = LocalDateTime.Format {
        amPmHour()
        char(':')
        minute()
        amPmMarker("am", "pm")
    }

    // 24 h clock, e.g. "10:15", used inside message bubbles (date separators carry the day)
    private val clockFormat = LocalDateTime.Format {
        hour()
        char(':')
        minute()
    }

    private val shortDateFormat = LocalDate.Format {
        day()
        char('.')
        monthNumber()
        char('.')
    }

    private val dateFormat = LocalDate.Format {
        day()
        char('/')
        monthNumber()
        char('/')
        year()
    }

    fun formatMessageTime(instant: Instant, clock: Clock = Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val messageDateTime = instant.toLocalDateTime(timeZone)
        val todayDate = clock.now().toLocalDateTime(timeZone).date
        val yesterdayDate = todayDate.minus(1, DateTimeUnit.DAY)

        val formattedTime = messageDateTime.format(timeFormat)

        return when(messageDateTime.date) {
            todayDate -> UiText.Resource(Res.string.today_x, arrayOf(formattedTime))
            yesterdayDate -> UiText.Resource(Res.string.yesterday_x, arrayOf(formattedTime))
            else -> UiText.DynamicString("${messageDateTime.date.format(dateFormat)}, $formattedTime")
        }
    }

    fun formatDateSeparator(date: LocalDate, clock: Clock = Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val today = clock.now().toLocalDateTime(timeZone).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        return when(date) {
            today -> UiText.Resource(Res.string.today)
            yesterday -> UiText.Resource(Res.string.yesterday)
            else -> UiText.DynamicString(date.format(dateFormat))
        }
    }

    /**
     * Time of a message inside its bubble.
     */
    fun formatBubbleTime(instant: Instant): UiText {
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return UiText.DynamicString(dateTime.format(clockFormat))
    }

    /**
     * Time of the last message in the chat list: clock time today, "Yesterday", otherwise the date.
     */
    fun formatChatListTime(instant: Instant, clock: Clock = Clock.System): UiText {
        val timeZone = TimeZone.currentSystemDefault()
        val dateTime = instant.toLocalDateTime(timeZone)
        val today = clock.now().toLocalDateTime(timeZone).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        return when(dateTime.date) {
            today -> UiText.DynamicString(dateTime.format(clockFormat))
            yesterday -> UiText.Resource(Res.string.yesterday)
            else -> UiText.DynamicString(dateTime.date.format(shortDateFormat))
        }
    }
}
