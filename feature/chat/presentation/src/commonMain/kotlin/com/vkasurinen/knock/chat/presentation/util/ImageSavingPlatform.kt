package com.vkasurinen.knock.chat.presentation.util

import org.jetbrains.compose.resources.StringResource

/** "Save to Gallery" on Android, "Save to Photos" on iOS, "Save to Pictures" on desktop */
expect val saveImageLabel: StringResource

/** Only Android 9 and lower need a runtime permission to add images to the gallery */
expect val isStoragePermissionRequiredToSaveImages: Boolean
