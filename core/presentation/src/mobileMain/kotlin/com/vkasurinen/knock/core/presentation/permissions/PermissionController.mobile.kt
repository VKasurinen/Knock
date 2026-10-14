package com.vkasurinen.knock.core.presentation.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import dev.icerock.moko.permissions.microphone.RECORD_AUDIO
import dev.icerock.moko.permissions.storage.WRITE_STORAGE

actual class PermissionController(
    private val mokoPermissionsController: PermissionsController
) {
    actual suspend fun requestPermission(permission: Permission): PermissionState {
        return try {
            mokoPermissionsController.providePermission(permission.toMokoPermission())
            PermissionState.GRANTED
        } catch (_: DeniedAlwaysException) {
            PermissionState.PERMANENTLY_DENIED
        } catch(_: DeniedException) {
            PermissionState.DENIED
        } catch(_: RequestCanceledException) {
            PermissionState.DENIED
        }
    }
}

fun Permission.toMokoPermission(): dev.icerock.moko.permissions.Permission {
    return when(this) {
        Permission.NOTIFICATIONS -> dev.icerock.moko.permissions.Permission.REMOTE_NOTIFICATION
        Permission.WRITE_STORAGE -> dev.icerock.moko.permissions.Permission.WRITE_STORAGE
        Permission.RECORD_AUDIO -> dev.icerock.moko.permissions.Permission.RECORD_AUDIO
    }
}