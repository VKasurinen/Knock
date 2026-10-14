package com.vkasurinen.knock.chat.presentation.util

import android.os.Build
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.save_to_gallery
import org.jetbrains.compose.resources.StringResource

actual val saveImageLabel: StringResource
    get() = Res.string.save_to_gallery

actual val isStoragePermissionRequiredToSaveImages: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
