package com.vkasurinen.knock.chat.data.attachment

import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.coroutines.test.runTest
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DesktopImageCompressorTest {

    private lateinit var userHome: File
    private var originalUserHome: String? = null

    private lateinit var compressor: DesktopImageCompressor

    @BeforeTest
    fun setUp() {
        // AttachmentFileStorage writes into the app data directory under the user's home
        originalUserHome = System.getProperty("user.home")
        userHome = Files.createTempDirectory("knock-home").toFile()
        System.setProperty("user.home", userHome.absolutePath)

        compressor = DesktopImageCompressor(AttachmentFileStorage(), NoOpLogger)
    }

    @AfterTest
    fun tearDown() {
        System.setProperty("user.home", originalUserHome!!)
        userHome.deleteRecursively()
    }

    @Test
    fun largeImage_isScaledDownToMaxDimension() = runTest {
        val result = compressor.compress(png(width = 4000, height = 3000), "holiday.png")

        val attachment = assertIs<Result.Success<*>>(result).data as OutgoingAttachment
        assertEquals(MAX_IMAGE_DIMENSION, attachment.width)
        assertEquals(1440, attachment.height)
    }

    @Test
    fun smallImage_keepsItsSize() = runTest {
        val result = compressor.compress(png(width = 300, height = 200), "small.png")

        val attachment = assertIs<Result.Success<*>>(result).data as OutgoingAttachment
        assertEquals(300, attachment.width)
        assertEquals(200, attachment.height)
    }

    @Test
    fun imageWithTransparency_isStoredAsJpeg() = runTest {
        val result = compressor.compress(png(width = 300, height = 200), "logo.png")

        val attachment = assertIs<Result.Success<*>>(result).data as OutgoingAttachment
        val file = File(attachment.localPath)
        assertEquals(AttachmentType.IMAGE, attachment.type)
        assertEquals("image/jpeg", attachment.mimeType)
        assertEquals("logo.jpg", attachment.fileName)
        assertEquals(file.length(), attachment.sizeBytes)
        assertEquals("jpeg", ImageIO.getImageReaders(ImageIO.createImageInputStream(file)).next().formatName.lowercase())
    }

    @Test
    fun invalidBytes_fail() = runTest {
        val result = compressor.compress("not an image".encodeToByteArray(), "broken.png")

        assertTrue(result is Result.Failure)
    }

    private fun png(width: Int, height: Int): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "png", output)
            output.toByteArray()
        }
    }

    private object NoOpLogger: KnockLogger {
        override fun debug(message: String) = Unit
        override fun info(message: String) = Unit
        override fun warn(message: String) = Unit
        override fun error(message: String, throwable: Throwable?) = Unit
    }
}
