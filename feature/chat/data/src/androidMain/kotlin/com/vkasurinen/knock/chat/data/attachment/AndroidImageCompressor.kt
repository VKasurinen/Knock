package com.vkasurinen.knock.chat.data.attachment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.vkasurinen.knock.chat.domain.attachment.ImageCompressor
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

class AndroidImageCompressor(
    private val fileStorage: AttachmentFileStorage,
    private val logger: KnockLogger
): ImageCompressor {

    override suspend fun compress(
        bytes: ByteArray,
        originalFileName: String?
    ): Result<OutgoingAttachment, DataError.Local> = withContext(Dispatchers.Default) {
        try {
            val bitmap = decodeScaledBitmap(bytes)
                ?: return@withContext Result.Failure(DataError.Local.UNKNOWN)

            val compressed = ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                output.toByteArray()
            }

            val path = fileStorage.newFilePath("jpg")
            fileStorage.write(path, compressed)

            Result.Success(
                OutgoingAttachment(
                    type = AttachmentType.IMAGE,
                    localPath = path,
                    fileName = jpegFileName(originalFileName),
                    mimeType = "image/jpeg",
                    sizeBytes = compressed.size.toLong(),
                    width = bitmap.width,
                    height = bitmap.height
                )
            ).also {
                bitmap.recycle()
            }
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

    private fun decodeScaledBitmap(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if(bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        // Decode at the smallest power of two that still covers the target size to save memory
        var sampleSize = 1
        while(max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        ) ?: return null

        val scale = MAX_IMAGE_DIMENSION.toFloat() / max(decoded.width, decoded.height)
        val rotation = ExifInterface(ByteArrayInputStream(bytes)).rotationDegrees
        if(scale >= 1f && rotation == 0) {
            return decoded
        }

        val matrix = Matrix().apply {
            if(scale < 1f) {
                postScale(scale, scale)
            }
            postRotate(rotation.toFloat())
        }
        return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true).also {
            if(it != decoded) {
                decoded.recycle()
            }
        }
    }
}
