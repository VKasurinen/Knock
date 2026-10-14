package com.vkasurinen.knock.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.draganddrop.DragAndDropTarget

/**
 * Accepts an image file dropped onto the app. Only desktop supports it, on mobile nothing is dropped.
 *
 * @param onHover whether a file is being dragged over the window
 */
@Composable
expect fun rememberDragAndDropTarget(
    onHover: (Boolean) -> Unit,
    onDrop: (PickedImageData) -> Unit
): DragAndDropTarget

/**
 * Whether files can be dropped onto the app, so the UI only hints at it where it works.
 */
expect val isDragAndDropSupported: Boolean
