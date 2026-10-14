package com.vkasurinen.knock.chat.data.attachment

import kotlin.time.Clock

internal const val MAX_IMAGE_DIMENSION = 1920
internal const val JPEG_QUALITY = 80
const val STORAGE_HTTP_CLIENT = "storage_http_client"

/**
 * Compressed images are always JPEG, so the extension of the original name is replaced.
 */
internal fun jpegFileName(originalFileName: String?): String {
    val baseName = originalFileName
        ?.substringBeforeLast('.')
        ?.takeIf { it.isNotBlank() }
        ?: "IMG_${Clock.System.now().toEpochMilliseconds()}"
    return "$baseName.jpg"
}
