package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.runtime.Composable
import com.vkasurinen.knock.chat.domain.audio.PlaybackState
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.chat.presentation.util.images
import com.vkasurinen.knock.chat.presentation.util.voiceMessage

/**
 * Callbacks for the attachments shown in message bubbles.
 */
class AttachmentCallbacks(
    val onImageClick: (images: List<MessageAttachment>, index: Int) -> Unit,
    val onVoiceMessagePlayPauseClick: (messageId: String, voiceMessage: MessageAttachment) -> Unit,
    val onVoiceMessageSeek: (messageId: String, voiceMessage: MessageAttachment, positionMs: Long) -> Unit
)

/**
 * The bubble content for a message's attachments, or null when it has none.
 */
fun messageAttachmentContent(
    messageId: String,
    attachments: List<MessageAttachment>,
    playbackState: PlaybackState,
    callbacks: AttachmentCallbacks,
    uploadProgress: Float? = null
): (@Composable () -> Unit)? {
    val voiceMessage = attachments.voiceMessage()
    if(voiceMessage != null) {
        val isLoaded = playbackState.id == messageId
        return {
            VoiceMessagePlayer(
                isPlaying = isLoaded && playbackState.isPlaying,
                positionMs = if(isLoaded) playbackState.positionMs else 0L,
                durationMs = playbackState.durationMs
                    .takeIf { isLoaded && it > 0 }
                    ?: voiceMessage.durationMs
                    ?: 0L,
                onPlayPauseClick = {
                    callbacks.onVoiceMessagePlayPauseClick(messageId, voiceMessage)
                },
                onSeek = { positionMs ->
                    callbacks.onVoiceMessageSeek(messageId, voiceMessage, positionMs)
                },
                uploadProgress = uploadProgress
            )
        }
    }

    val images = attachments.images()
    if(images.isNotEmpty()) {
        return {
            MessageImageGrid(
                images = images,
                onImageClick = { index -> callbacks.onImageClick(images, index) },
                uploadProgress = uploadProgress
            )
        }
    }
    return null
}
