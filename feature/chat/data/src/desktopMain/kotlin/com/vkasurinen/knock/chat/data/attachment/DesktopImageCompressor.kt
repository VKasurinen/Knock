package com.vkasurinen.knock.chat.data.attachment

import com.vkasurinen.knock.chat.domain.attachment.ImageCompressor
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.math.max
import kotlin.math.roundToInt

class DesktopImageCompressor(
    private val fileStorage: AttachmentFileStorage,
    private val logger: KnockLogger
): ImageCompressor {

    override suspend fun compress(
        bytes: ByteArray,
        originalFileName: String?
    ): Result<OutgoingAttachment, DataError.Local> = withContext(Dispatchers.Default) {
        try {
            val decoded = ImageIO.read(ByteArrayInputStream(bytes))
                ?: return@withContext Result.Failure(DataError.Local.UNKNOWN)
            val image = scaleToRgb(decoded)
            val compressed = encodeJpeg(image)

            val path = fileStorage.newFilePath("jpg")
            fileStorage.write(path, compressed)

            Result.Success(
                OutgoingAttachment(
                    type = AttachmentType.IMAGE,
                    localPath = path,
                    fileName = jpegFileName(originalFileName),
                    mimeType = "image/jpeg",
                    sizeBytes = compressed.size.toLong(),
                    width = image.width,
                    height = image.height
                )
            )
        } catch(e: CancellationException) {
            throw e
        } catch(e: Exception) {
            logger.error("Failed to compress image", e)
            Result.Failure(DataError.Local.UNKNOWN)
        } catch(e: OutOfMemoryError) {
            logger.error("Image too large to compress", e)
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }

    /**
     * Downscales to [MAX_IMAGE_DIMENSION] and drops the alpha channel, which JPEG can't store.
     */
    private fun scaleToRgb(source: BufferedImage): BufferedImage {
        val scale = minOf(1f, MAX_IMAGE_DIMENSION.toFloat() / max(source.width, source.height))
        val width = (source.width * scale).roundToInt().coerceAtLeast(1)
        val height = (source.height * scale).roundToInt().coerceAtLeast(1)

        return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
            val graphics = createGraphics()
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                graphics.drawImage(source, 0, 0, width, height, java.awt.Color.WHITE, null)
            } finally {
                graphics.dispose()
            }
        }
    }

    private fun encodeJpeg(image: BufferedImage): ByteArray {
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        return try {
            ByteArrayOutputStream().use { output ->
                ImageIO.createImageOutputStream(output).use { imageOutput ->
                    writer.output = imageOutput
                    val params = writer.defaultWriteParam.apply {
                        compressionMode = ImageWriteParam.MODE_EXPLICIT
                        compressionQuality = JPEG_QUALITY / 100f
                    }
                    writer.write(null, IIOImage(image, null, null), params)
                }
                output.toByteArray()
            }
        } finally {
            writer.dispose()
        }
    }
}
