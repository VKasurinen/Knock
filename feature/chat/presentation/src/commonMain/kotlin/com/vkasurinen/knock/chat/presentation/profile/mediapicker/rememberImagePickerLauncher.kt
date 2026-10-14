package com.vkasurinen.knock.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable

/**
 * @param maxItems how many images the user can pick at once
 * @param onResult the picked images in the order they were selected, never empty
 */
@Composable
expect fun rememberImagePickerLauncher(
    maxItems: Int = 1,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher

class ImagePickerLauncher(
    private val onLaunch: () -> Unit
) {
    fun launch() {
        onLaunch()
    }
}

class PickedImageData(
    val bytes: ByteArray,
    val mimeType: String?,
    val fileName: String? = null
)
