package com.vkasurinen.knock.chat.data.attachment

import com.vkasurinen.knock.core.data.util.appDataDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class AttachmentFileStorage {

    private val directory: File
        get() = File(appDataDirectory, "attachments").apply { mkdirs() }

    @OptIn(ExperimentalUuidApi::class)
    actual fun newFilePath(extension: String): String {
        return File(directory, "${Uuid.random()}.$extension").absolutePath
    }

    actual suspend fun write(path: String, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            File(path).writeBytes(bytes)
        }
    }

    actual suspend fun read(path: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            File(path).takeIf { it.exists() }?.readBytes()
        }
    }
}
