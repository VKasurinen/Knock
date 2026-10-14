@file:OptIn(ExperimentalForeignApi::class)

package com.vkasurinen.knock.chat.data.attachment

import com.vkasurinen.knock.chat.domain.attachment.ImageCompressor
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class IosImageCompressor(
    private val fileStorage: AttachmentFileStorage,
    private val logger: KnockLogger
): ImageCompressor {

    override suspend fun compress(
        bytes: ByteArray,
        originalFileName: String?
    ): Result<OutgoingAttachment, DataError.Local> = withContext(Dispatchers.Default) {
        try {
            val image = UIImage(data = bytes.toNSData())
            val (originalWidth, originalHeight) = image.size.useContents { width to height }
            if(originalWidth <= 0.0 || originalHeight <= 0.0) {
                return@withContext Result.Failure(DataError.Local.UNKNOWN)
            }

            val scale = min(1.0, MAX_IMAGE_DIMENSION / max(originalWidth, originalHeight))
            val width = (originalWidth * scale).roundToInt()
            val height = (originalHeight * scale).roundToInt()

            // Drawing also applies the EXIF orientation, so the result is always upright
            val format = UIGraphicsImageRendererFormat.defaultFormat().apply {
                setScale(1.0)
            }
            val renderer = UIGraphicsImageRenderer(
                size = CGSizeMake(width.toDouble(), height.toDouble()),
                format = format
            )
            val resized = renderer.imageWithActions { _ ->
                image.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
            }

            val jpeg = UIImageJPEGRepresentation(resized, JPEG_QUALITY / 100.0)
                ?.toByteArray()
                ?: return@withContext Result.Failure(DataError.Local.UNKNOWN)

            val path = fileStorage.newFilePath("jpg")
            fileStorage.write(path, jpeg)

            Result.Success(
                OutgoingAttachment(
                    type = AttachmentType.IMAGE,
                    localPath = path,
                    fileName = jpegFileName(originalFileName),
                    mimeType = "image/jpeg",
                    sizeBytes = jpeg.size.toLong(),
                    width = width,
                    height = height
                )
            )
        } catch(e: CancellationException) {
            throw e
        } catch(e: Exception) {
            logger.error("Failed to compress image", e)
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }
}
