@file:OptIn(ExperimentalForeignApi::class)

package com.vkasurinen.knock.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationSelectionOrdered
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UniformTypeIdentifiers.UTType
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_group_create
import platform.darwin.dispatch_group_enter
import platform.darwin.dispatch_group_leave
import platform.darwin.dispatch_group_notify
import platform.posix.memcpy

@Composable
actual fun rememberImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)

    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)

                val results = didFinishPicking.filterIsInstance<PHPickerResult>()
                if(results.isEmpty()) {
                    return
                }

                // Results load in parallel, so collect them by position to keep the selection order
                val pickedImages = arrayOfNulls<PickedImageData>(results.size)
                val dispatchGroup = dispatch_group_create()

                results.forEachIndexed { index, result ->
                    val itemProvider = result.itemProvider
                    val primaryType = itemProvider.registeredTypeIdentifiers.firstOrNull() as? String
                        ?: return@forEachIndexed
                    val mimeType = UTType.typeWithIdentifier(primaryType)?.preferredMIMEType
                        ?: return@forEachIndexed

                    dispatch_group_enter(dispatchGroup)
                    itemProvider.loadDataRepresentationForTypeIdentifier(
                        typeIdentifier = primaryType
                    ) { nsData, _ ->
                        scope.launch {
                            nsData?.let {
                                val bytes = ByteArray(it.length.toInt())
                                withContext(Dispatchers.Default) {
                                    memcpy(bytes.refTo(0), it.bytes, it.length)
                                }
                                pickedImages[index] = PickedImageData(
                                    bytes = bytes,
                                    mimeType = mimeType,
                                    fileName = itemProvider.suggestedName
                                )
                            }
                            dispatch_group_leave(dispatchGroup)
                        }
                    }
                }

                dispatch_group_notify(dispatchGroup, dispatch_get_main_queue()) {
                    val images = pickedImages.filterNotNull()
                    if(images.isNotEmpty()) {
                        currentOnResult(images)
                    }
                }
            }
        }
    }

    return remember(maxItems) {
        ImagePickerLauncher(
            onLaunch = {
                // A picker view controller can only be presented once, so create one per launch
                val pickerViewController = PHPickerViewController(
                    configuration = PHPickerConfiguration().apply {
                        setSelectionLimit(maxItems.toLong())
                        setFilter(PHPickerFilter.imagesFilter)
                        setSelection(PHPickerConfigurationSelectionOrdered)
                    }
                )
                pickerViewController.delegate = delegate
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    pickerViewController,
                    true,
                    null
                )
            }
        )
    }
}
