package com.vkasurinen.knock.core.presentation.util

import knock.core.presentation.generated.resources.Res
import knock.core.presentation.generated.resources.error_not_found
import knock.core.presentation.generated.resources.error_unknown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UiTextTest {

    @Test
    fun resourcesWithEqualArguments_areEqual() {
        val first = UiText.Resource(Res.string.error_unknown, arrayOf("12:30"))
        val second = UiText.Resource(Res.string.error_unknown, arrayOf("12:30"))

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun resourcesWithDifferentArguments_areNotEqual() {
        assertNotEquals<UiText>(
            UiText.Resource(Res.string.error_unknown, arrayOf("12:30")),
            UiText.Resource(Res.string.error_unknown, arrayOf("12:31"))
        )
    }

    @Test
    fun differentResources_areNotEqual() {
        assertNotEquals<UiText>(
            UiText.Resource(Res.string.error_unknown),
            UiText.Resource(Res.string.error_not_found)
        )
    }
}
