package com.vkasurinen.knock.chat.presentation.util

import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.save_to_pictures
import org.jetbrains.compose.resources.StringResource

actual val saveImageLabel: StringResource
    get() = Res.string.save_to_pictures

actual val isStoragePermissionRequiredToSaveImages: Boolean
    get() = false
