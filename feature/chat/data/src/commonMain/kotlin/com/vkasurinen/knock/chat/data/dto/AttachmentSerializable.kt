package com.vkasurinen.knock.chat.data.dto

import kotlinx.serialization.Serializable

/**
 * How attachments are stored in the local database, as a JSON array on the message row.
 */
@Serializable
data class AttachmentSerializable(
    val id: String? = null,
    val type: String,
    val url: String? = null,
    val localPath: String? = null,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)
