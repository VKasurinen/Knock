@file:OptIn(ExperimentalComposeUiApi::class)

package com.vkasurinen.knock.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
actual fun rememberDragAndDropTarget(
    onHover: (Boolean) -> Unit,
    onDrop: (PickedImageData) -> Unit
): DragAndDropTarget {
    val scope = rememberCoroutineScope()
    val currentOnHover by rememberUpdatedState(onHover)
    val currentOnDrop by rememberUpdatedState(onDrop)

    return remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                currentOnHover(true)
            }

            override fun onEnded(event: DragAndDropEvent) {
                currentOnHover(false)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val file = event.droppedImageFile() ?: return false

                scope.launch {
                    val image = withContext(Dispatchers.IO) {
                        file.toPickedImageData()
                    }
                    if(image != null) {
                        currentOnDrop(image)
                    }
                }
                return true
            }
        }
    }
}


private fun DragAndDropEvent.droppedImageFile(): File? {
    val transferable = awtTransferable
    if(!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        return null
    }
    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>
    val file = files?.singleOrNull() as? File ?: return null
    return file.takeIf { it.isImageFile() }
}

actual val isDragAndDropSupported: Boolean = true
