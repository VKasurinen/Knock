package com.vkasurinen.knock.chat.presentation.util

import androidx.compose.runtime.Composable
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.photo
import knock.feature.chat.presentation.generated.resources.voice_message
import knock.feature.chat.presentation.generated.resources.x_photos
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import org.jetbrains.compose.resources.stringResource

/**
 * What Coil should load for an attachment. The local copy is preferred, so the sender doesn't
 * need to download images they just sent.
 */
val MessageAttachment.imageModel: String?
    get() = mediaSource

/**
 * Where to load an attachment from, the local copy if there is one. Either a file:// URI or a URL.
 */
val MessageAttachment.mediaSource: String?
    get() = localPath?.let { path ->
        // Windows paths (C:\Users\...) need a slash before the drive and forward slashes
        if(path.startsWith("/")) "file://$path" else "file:///${path.replace('\\', '/')}"
    } ?: url

fun List<MessageAttachment>.voiceMessage(): MessageAttachment? {
    return firstOrNull { it.type == AttachmentType.AUDIO }
}

fun List<MessageAttachment>.images(): List<MessageAttachment> {
    return filter { it.type == AttachmentType.IMAGE }
}

/**
 * Short description of a message for previews, e.g. "Photo" when it only has an image.
 */
@Composable
fun ChatMessage.previewText(): String {
    if(content.isNotBlank()) {
        return content
    }
    val images = attachments.images()
    return when {
        attachments.any { it.type == AttachmentType.AUDIO } -> stringResource(Res.string.voice_message)
        images.size == 1 -> stringResource(Res.string.photo)
        images.size > 1 -> stringResource(Res.string.x_photos, images.size)
        else -> content
    }
}
