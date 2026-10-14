@file:OptIn(ExperimentalForeignApi::class)

package com.vkasurinen.knock.chat.data.attachment

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class AttachmentFileStorage {

    private val directory: String by lazy {
        val documents = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        val path = requireNotNull(documents?.path) + "/attachments"
        NSFileManager.defaultManager.createDirectoryAtPath(
            path = path,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        path
    }

    @OptIn(ExperimentalUuidApi::class)
    actual fun newFilePath(extension: String): String {
        return "$directory/${Uuid.random()}.$extension"
    }

    actual suspend fun write(path: String, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            bytes.toNSData().writeToFile(path, atomically = true)
        }
    }

    actual suspend fun read(path: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            NSData.dataWithContentsOfFile(path)?.toByteArray()
        }
    }
}
