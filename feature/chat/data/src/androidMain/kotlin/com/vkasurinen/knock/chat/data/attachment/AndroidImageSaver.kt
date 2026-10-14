package com.vkasurinen.knock.chat.data.attachment

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.vkasurinen.knock.chat.domain.attachment.ImageSaver
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.core.data.networking.safeCall
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Saves into Pictures/Knock. Below Android 10 this needs the WRITE_EXTERNAL_STORAGE permission,
 * which the UI requests before calling this.
 */
class AndroidImageSaver(
    private val context: Context,
    private val fileStorage: AttachmentFileStorage,
    private val storageHttpClient: HttpClient,
    private val logger: KnockLogger
): ImageSaver {

    override suspend fun saveToGallery(attachment: MessageAttachment): EmptyResult<DataError> {
        val bytes = when(val result = loadBytes(attachment)) {
            is Result.Failure -> return result
            is Result.Success -> result.data
        }

        return withContext(Dispatchers.IO) {
            try {
                writeToMediaStore(attachment, bytes)
            } catch(e: CancellationException) {
                throw e
            } catch(e: Exception) {
                logger.error("Failed to save image to gallery", e)
                Result.Failure(DataError.Local.UNKNOWN)
            }
        }
    }

    private suspend fun loadBytes(attachment: MessageAttachment): Result<ByteArray, DataError> {
        attachment.localPath
            ?.let { fileStorage.read(it) }
            ?.let { return Result.Success(it) }

        val url = attachment.url ?: return Result.Failure(DataError.Local.NOT_FOUND)
        return safeCall<ByteArray> {
            storageHttpClient.get(url)
        }
    }

    private fun writeToMediaStore(
        attachment: MessageAttachment,
        bytes: ByteArray
    ): EmptyResult<DataError> {
        val resolver = context.contentResolver
        val isScopedStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, attachment.fileName)
            put(MediaStore.Images.Media.MIME_TYPE, attachment.mimeType)
            if(isScopedStorage) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Knock")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if(isScopedStorage) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, values)
            ?: return Result.Failure(DataError.Local.UNKNOWN)

        val isWritten = resolver.openOutputStream(uri)?.use { output ->
            output.write(bytes)
            true
        } ?: false

        if(!isWritten) {
            resolver.delete(uri, null, null)
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        if(isScopedStorage) {
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null
            )
        }
        return Result.Success(Unit)
    }
}
