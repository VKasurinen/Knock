package com.vkasurinen.knock.chat.data.attachment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttachmentFileNamesTest {

    @Test
    fun extensionIsReplacedWithJpg() {
        assertEquals("holiday.jpg", jpegFileName("holiday.heic"))
    }

    @Test
    fun nameWithoutExtension_getsJpg() {
        assertEquals("holiday.jpg", jpegFileName("holiday"))
    }

    @Test
    fun missingName_isGenerated() {
        val name = jpegFileName(null)

        assertTrue(name.startsWith("IMG_"))
        assertTrue(name.endsWith(".jpg"))
    }
}
