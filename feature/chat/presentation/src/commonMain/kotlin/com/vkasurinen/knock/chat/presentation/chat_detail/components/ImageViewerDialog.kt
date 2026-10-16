package com.vkasurinen.knock.chat.presentation.chat_detail.components

import knock.core.designsystem.generated.resources.close_icon
import knock.core.designsystem.generated.resources.Res as DesignSystemRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.close
import knock.feature.chat.presentation.generated.resources.image_x_of_y
import coil3.compose.AsyncImage
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.chat.presentation.util.imageModel
import com.vkasurinen.knock.chat.presentation.util.saveImageLabel
import com.vkasurinen.knock.core.designsystem.components.dropdown.KnockDropDownMenu
import com.vkasurinen.knock.core.designsystem.components.dropdown.DropDownItem
import com.vkasurinen.knock.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import knock.feature.chat.presentation.generated.resources.download_icon

private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 2.5f

/**
 * Full screen viewer for a message's images. Swipe between images, pinch or double tap to zoom
 * and long press to save. Without [onSaveClick] there is no save menu, e.g. for profile pictures.
 */
@Composable
fun ImageViewerDialog(
    images: List<MessageAttachment>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSaveClick: ((MessageAttachment) -> Unit)? = null
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
        pageCount = { images.size }
    )
    var imageWithOpenMenu by remember { mutableStateOf<MessageAttachment?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                key = { index -> images[index].imageModel ?: index },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableImage(
                    image = images[page],
                    onLongPress = {
                        if(onSaveClick != null) {
                            imageWithOpenMenu = images[page]
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                if(images.size > 1) {
                    Text(
                        text = stringResource(
                            Res.string.image_x_of_y,
                            pagerState.currentPage + 1,
                            images.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(DesignSystemRes.drawable.close_icon),
                        contentDescription = stringResource(Res.string.close)
                    )
                }

                // Anchors the context menu in the middle of the screen
                Box(modifier = Modifier.align(Alignment.Center)) {
                    KnockDropDownMenu(
                        isOpen = imageWithOpenMenu != null,
                        onDismiss = { imageWithOpenMenu = null },
                        items = listOf(
                            DropDownItem(
                                title = stringResource(saveImageLabel),
                                icon = vectorResource(Res.drawable.download_icon),
                                contentColor = MaterialTheme.colorScheme.extended.textPrimary,
                                onClick = {
                                    imageWithOpenMenu?.let { onSaveClick?.invoke(it) }
                                    imageWithOpenMenu = null
                                }
                            )
                        )
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    image: MessageAttachment,
    onLongPress: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun clampOffset(value: Offset): Offset {
        val maxX = size.width * (scale - 1f) / 2f
        val maxY = size.height * (scale - 1f) / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    AsyncImage(
        model = image.imageModel,
        contentDescription = image.fileName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if(scale > 1f) 1f else DOUBLE_TAP_ZOOM
                        offset = clampOffset(offset)
                    },
                    onLongPress = { onLongPress() }
                )
            }
            .pointerInput(Unit) {
                // Only consumes the gesture while zooming or zoomed in, so the pager can
                // still be swiped with one finger
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val isPinching = event.changes.size > 1
                        if(isPinching || scale > 1f) {
                            scale = (scale * event.calculateZoom()).coerceIn(1f, MAX_ZOOM)
                            offset = clampOffset(offset + event.calculatePan())
                            event.changes.forEach { change ->
                                if(change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        }
                    } while(event.changes.any { it.pressed })
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    )
}
