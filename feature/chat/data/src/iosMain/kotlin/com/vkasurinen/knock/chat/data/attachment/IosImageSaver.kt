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
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume

/**
 * Adds the image to the photo library. The system asks for add-only access on first use
 * (NSPhotoLibraryAddUsageDescription in Info.plist).
 */
class IosImageSaver(
    private val fileStorage: AttachmentFileStorage,
    private val storageHttpClient: HttpClient,
    private val logger: KnockLogger
): ImageSaver {

    override suspend fun saveToGallery(attachment: MessageAttachment): EmptyResult<DataError> {
        val bytes = when(val result = loadBytes(attachment)) {
            is Result.Failure -> return result
            is Result.Success -> result.data
        }
        val data = bytes.toNSData()

        return suspendCancellableCoroutine { continuation ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = {
                    PHAssetCreationRequest
                        .creationRequestForAsset()
                        .addResourceWithType(PHAssetResourceTypePhoto, data = data, options = null)
                },
                completionHandler = { success, error ->
                    if(success) {
                        continuation.resume(Result.Success(Unit))
                    } else {
                        logger.error("Failed to save image to photos: ${error?.localizedDescription}")
                        continuation.resume(Result.Failure(DataError.Local.UNKNOWN))
                    }
                }
            )
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
}
