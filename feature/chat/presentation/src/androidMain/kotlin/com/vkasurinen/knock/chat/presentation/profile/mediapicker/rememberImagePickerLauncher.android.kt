package com.vkasurinen.knock.chat.presentation.profile.mediapicker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
actual fun rememberImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun handleUris(uris: List<Uri>) {
        if(uris.isEmpty()) {
            return
        }
        val parser = ContentUriParser(context)
        scope.launch {
            val images = uris.mapNotNull { uri ->
                PickedImageData(
                    bytes = parser.readUri(uri) ?: return@mapNotNull null,
                    mimeType = parser.getMimeType(uri),
                    fileName = parser.getFileName(uri)
                )
            }
            if(images.isNotEmpty()) {
                onResult(images)
            }
        }
    }

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        handleUris(listOfNotNull(uri))
    }
    // PickMultipleVisualMedia requires a limit of at least 2
    val multiplePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems.coerceAtLeast(2))
    ) { uris ->
        handleUris(uris)
    }

    return remember(maxItems) {
        ImagePickerLauncher(
            onLaunch = {
                val request = PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
                if(maxItems > 1) {
                    multiplePickerLauncher.launch(request)
                } else {
                    singlePickerLauncher.launch(request)
                }
            }
        )
    }
}
