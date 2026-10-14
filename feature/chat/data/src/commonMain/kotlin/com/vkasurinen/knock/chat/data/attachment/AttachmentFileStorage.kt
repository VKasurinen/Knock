package com.vkasurinen.knock.chat.data.attachment

/**
 * Local copies of attachments the user sends. They are uploaded from here, kept for retries
 * and shown in the chat without having to download them again.
 */
expect class AttachmentFileStorage {
    fun newFilePath(extension: String): String
    suspend fun write(path: String, bytes: ByteArray)
    suspend fun read(path: String): ByteArray?
}
