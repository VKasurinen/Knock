package com.vkasurinen.knock.chat.data.attachment

import com.vkasurinen.knock.chat.domain.attachment.ImageSaver
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.core.data.networking.safeCall
import com.vkasurinen.knock.core.domain.logging.KnockLogger
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Saves into ~/Pictures/Knock. Desktop has no gallery, so the Pictures folder is the closest match.
 */
class DesktopImageSaver(
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
                val directory = File(System.getProperty("user.home"), "Pictures/Knock").apply { mkdirs() }
                uniqueFile(directory, attachment.fileName).writeBytes(bytes)
                Result.Success(Unit)
            } catch(e: CancellationException) {
                throw e
            } catch(e: Exception) {
                logger.error("Failed to save image to Pictures", e)
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

    /**
     * Adds " (1)", " (2)", ... before the extension instead of overwriting an earlier save.
     */
    private fun uniqueFile(directory: File, fileName: String): File {
        val name = File(fileName).name
        val baseName = name.substringBeforeLast('.')
        val extension = name.substringAfterLast('.', missingDelimiterValue = "")
            .let { if(it.isEmpty()) "" else ".$it" }

        var file = File(directory, name)
        var index = 1
        while(file.exists()) {
            file = File(directory, "$baseName ($index)$extension")
            index++
        }
        return file
    }
}
