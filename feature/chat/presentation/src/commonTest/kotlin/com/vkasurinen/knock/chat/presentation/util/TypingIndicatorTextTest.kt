package com.vkasurinen.knock.chat.presentation.util

import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.typing_one
import knock.feature.chat.presentation.generated.resources.typing_several
import knock.feature.chat.presentation.generated.resources.typing_three
import knock.feature.chat.presentation.generated.resources.typing_two
import com.vkasurinen.knock.core.presentation.util.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class TypingIndicatorTextTest {

    @Test
    fun nobodyTyping_returnsNull() {
        assertNull(typingIndicatorText(emptyList()))
    }

    @Test
    fun oneUser_showsTheirName() {
        val text = assertIs<UiText.Resource>(typingIndicatorText(listOf("Alice")))

        assertEquals(Res.string.typing_one, text.id)
        assertEquals(listOf("Alice"), text.args.toList())
    }

    @Test
    fun twoUsers_showsBothNames() {
        val text = assertIs<UiText.Resource>(typingIndicatorText(listOf("Alice", "Bob")))

        assertEquals(Res.string.typing_two, text.id)
        assertEquals(listOf("Alice", "Bob"), text.args.toList())
    }

    @Test
    fun threeUsers_showsAllNames() {
        val text = assertIs<UiText.Resource>(typingIndicatorText(listOf("Alice", "Bob", "Carol")))

        assertEquals(Res.string.typing_three, text.id)
        assertEquals(listOf("Alice", "Bob", "Carol"), text.args.toList())
    }

    @Test
    fun moreThanThreeUsers_showsSeveralPeople() {
        val text = assertIs<UiText.Resource>(
            typingIndicatorText(listOf("Alice", "Bob", "Carol", "Dave"))
        )

        assertEquals(Res.string.typing_several, text.id)
        assertEquals(0, text.args.size)
    }
}
