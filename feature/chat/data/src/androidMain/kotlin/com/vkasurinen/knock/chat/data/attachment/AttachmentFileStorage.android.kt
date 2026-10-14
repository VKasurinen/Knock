package com.vkasurinen.knock.chat.data.attachment

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class AttachmentFileStorage(
    private val context: Context
) {
    private val directory: File
        get() = File(context.filesDir, "attachments").apply { mkdirs() }

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
