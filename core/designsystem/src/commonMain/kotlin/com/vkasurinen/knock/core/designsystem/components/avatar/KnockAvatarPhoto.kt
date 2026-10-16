package com.vkasurinen.knock.core.designsystem.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vkasurinen.knock.core.designsystem.theme.KnockTheme
import com.vkasurinen.knock.core.designsystem.theme.extended
import com.vkasurinen.knock.core.designsystem.theme.labelXSmall
import com.vkasurinen.knock.core.designsystem.theme.titleXSmall


enum class AvatarSize(val dp: Dp) {
    XSMALL(32.dp), SMALL(40.dp), LARGE(56.dp), XLARGE(96.dp)
}

@Composable
fun KnockAvatarPhoto(
    displayText: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.SMALL,
    imageUrl: String? = null,
    /** Anything Coil can load, e.g. a picked image's bytes. Defaults to [imageUrl] */
    imageModel: Any? = imageUrl,
    onClick: (() -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.extended.onAvatar,
    backgroundColor: Color = MaterialTheme.colorScheme.extended.avatarBackground
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .clickable(
                onClick = { onClick?.invoke() },
                enabled = onClick != null
            )
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.extended.surfaceOutline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText.uppercase(),
            style = when(size) {
                AvatarSize.XSMALL -> MaterialTheme.typography.labelXSmall
                AvatarSize.SMALL -> MaterialTheme.typography.titleXSmall
                AvatarSize.LARGE -> MaterialTheme.typography.titleSmall
                AvatarSize.XLARGE -> MaterialTheme.typography.titleLarge
            },
            color = textColor
        )
        AsyncImage(
            model = imageModel,
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .matchParentSize()
        )
    }
}

@Composable
@Preview
fun KnockAvatarPhotoPreview() {
    KnockTheme {
        KnockAvatarPhoto(
            displayText = "PL",
            size = AvatarSize.LARGE
        )
    }
}


@Composable
@Preview (
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
fun KnockAvatarPhotoPreview2() {
    KnockTheme {
        KnockAvatarPhoto(
            displayText = "PL",
            size = AvatarSize.LARGE
        )
    }
}