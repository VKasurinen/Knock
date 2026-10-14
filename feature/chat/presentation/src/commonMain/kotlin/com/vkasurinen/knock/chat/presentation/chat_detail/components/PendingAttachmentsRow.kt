package com.vkasurinen.knock.chat.presentation.chat_detail.components

import org.jetbrains.compose.resources.vectorResource
import knock.core.designsystem.generated.resources.close_icon
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.remove_attachment
import coil3.compose.AsyncImage
import com.vkasurinen.knock.chat.presentation.model.PendingAttachmentUi
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

private val THUMBNAIL_SIZE = 72.dp
private val REMOVE_BUTTON_OVERLAP = 8.dp

@Composable
fun PendingAttachmentsRow(
    attachments: List<PendingAttachmentUi>,
    onRemoveClick: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(
            items = attachments,
            key = { it.id }
        ) { attachment ->
            PendingAttachmentItem(
                attachment = attachment,
                onRemoveClick = { onRemoveClick(attachment.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun PendingAttachmentItem(
    attachment: PendingAttachmentUi,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The remove button overlaps the thumbnail's top right corner
    Box(
        modifier = modifier.size(THUMBNAIL_SIZE + REMOVE_BUTTON_OVERLAP)
    ) {
        val thumbnailModifier = Modifier
            .align(Alignment.BottomStart)
            .size(THUMBNAIL_SIZE)

        when(val state = attachment.state) {
            is PendingAttachmentUi.State.Ready -> {
                AsyncImage(
                    model = "file://${state.attachment.localPath}",
                    contentDescription = attachment.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = thumbnailModifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.extended.surfaceLower)
                )
            }
            PendingAttachmentUi.State.Processing,
            PendingAttachmentUi.State.Failed -> {
                AttachmentFileChip(
                    fileName = attachment.fileName,
                    isError = state is PendingAttachmentUi.State.Failed,
                    modifier = thumbnailModifier
                )
            }
        }

        IconButton(
            onClick = onRemoveClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.extended.textPrimary,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = vectorResource(DesignSystemRes.drawable.close_icon),
                contentDescription = stringResource(Res.string.remove_attachment),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
