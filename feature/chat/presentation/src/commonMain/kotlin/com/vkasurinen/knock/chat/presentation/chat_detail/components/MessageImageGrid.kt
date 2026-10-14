package com.vkasurinen.knock.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.chat.presentation.util.imageModel
import com.vkasurinen.knock.core.designsystem.theme.extended

private val SINGLE_IMAGE_WIDTH = 220.dp
private val GRID_IMAGE_SIZE = 108.dp
private val GRID_SPACING = 4.dp

/**
 * Image attachments of a message: one large image, or a two column grid of thumbnails.
 * Not lazy on purpose, the chat bubble measures its content intrinsically.
 */
@Composable
fun MessageImageGrid(
    images: List<MessageAttachment>,
    onImageClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    uploadProgress: Float? = null
) {
    Box(modifier = modifier) {
        if(images.size == 1) {
            val image = images.first()
            val aspectRatio = image.aspectRatio()
            MessageImage(
                image = image,
                onClick = { onImageClick(0) },
                width = SINGLE_IMAGE_WIDTH,
                height = SINGLE_IMAGE_WIDTH / aspectRatio
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(GRID_SPACING)) {
                images.chunked(2).forEachIndexed { rowIndex, rowImages ->
                    Row(horizontalArrangement = Arrangement.spacedBy(GRID_SPACING)) {
                        rowImages.forEachIndexed { columnIndex, image ->
                            val index = rowIndex * 2 + columnIndex
                            MessageImage(
                                image = image,
                                onClick = { onImageClick(index) },
                                width = GRID_IMAGE_SIZE,
                                height = GRID_IMAGE_SIZE
                            )
                        }
                    }
                }
            }
        }

        if(uploadProgress != null) {
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.extended.accentBrand,
                trackColor = MaterialTheme.colorScheme.extended.surfaceOutline
            )
        }
    }
}

@Composable
private fun MessageImage(
    image: MessageAttachment,
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    var imageState by remember(image.imageModel) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.extended.surfaceLower)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = image.imageModel,
            contentDescription = image.fileName,
            contentScale = ContentScale.Crop,
            onState = { imageState = it },
            modifier = Modifier.fillMaxSize()
        )
        if(imageState is AsyncImagePainter.State.Error) {
            AttachmentFileChip(
                fileName = image.fileName,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun MessageAttachment.aspectRatio(): Float {
    val width = width ?: return 1f
    val height = height ?: return 1f
    if(width <= 0 || height <= 0) {
        return 1f
    }
    // Keep very tall or wide images from taking over the chat
    return (width.toFloat() / height).coerceIn(0.6f, 1.8f)
}
