package com.vkasurinen.knock.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.attach_images
import knock.feature.chat.presentation.generated.resources.select_a_profile_picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@Composable
actual fun rememberImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    // The profile picture is the only place where a single image is picked
    val dialogTitle = stringResource(
        if(maxItems > 1) Res.string.attach_images else Res.string.select_a_profile_picture
    )

    return remember(maxItems, dialogTitle) {
        ImagePickerLauncher(
            onLaunch = {
                val files = showImageFileDialog(
                    title = dialogTitle,
                    isMultipleMode = maxItems > 1
                ).take(maxItems)
                if(files.isNotEmpty()) {
                    scope.launch {
                        val images = withContext(Dispatchers.IO) {
                            files.mapNotNull { it.toPickedImageData() }
                        }
                        if(images.isNotEmpty()) {
                            currentOnResult(images)
                        }
                    }
                }
            }
        )
    }
}

/**
 * Opens the native file dialog of the OS. Blocks until the user picks files or cancels.
 */
private fun showImageFileDialog(title: String, isMultipleMode: Boolean): List<File> {
    val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
        this.isMultipleMode = isMultipleMode
        // macOS and Linux use the filter, Windows only the file name pattern
        filenameFilter = FilenameFilter { _, name -> File(name).isImageFile() }
        file = IMAGE_MIME_TYPES.keys.joinToString(";") { "*.$it" }
        isVisible = true
    }
    return dialog.files.toList()
}

internal fun File.isImageFile(): Boolean {
    return extension.lowercase() in IMAGE_MIME_TYPES
}

internal fun File.toPickedImageData(): PickedImageData? {
    return try {
        PickedImageData(
            bytes = readBytes(),
            mimeType = IMAGE_MIME_TYPES[extension.lowercase()],
            fileName = name
        )
    } catch(_: Exception) {
        null
    }
}

// Formats that the JVM's ImageIO can decode, so they can be compressed before sending
private val IMAGE_MIME_TYPES = mapOf(
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "png" to "image/png",
    "gif" to "image/gif",
    "bmp" to "image/bmp"
)
