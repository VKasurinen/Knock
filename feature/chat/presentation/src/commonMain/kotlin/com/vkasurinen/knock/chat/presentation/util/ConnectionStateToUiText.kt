package com.vkasurinen.knock.chat.presentation.util

import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.network_error
import knock.feature.chat.presentation.generated.resources.offline
import knock.feature.chat.presentation.generated.resources.online
import knock.feature.chat.presentation.generated.resources.reconnecting
import knock.feature.chat.presentation.generated.resources.unknown_error
import com.vkasurinen.knock.chat.domain.models.ConnectionState
import com.vkasurinen.knock.core.presentation.util.UiText

fun ConnectionState.toUiText(): UiText {
    val resource = when(this) {
        ConnectionState.DISCONNECTED -> Res.string.offline
        ConnectionState.CONNECTING -> Res.string.reconnecting
        ConnectionState.CONNECTED -> Res.string.online
        ConnectionState.ERROR_NETWORK -> Res.string.network_error
        ConnectionState.ERROR_UNKNOWN -> Res.string.unknown_error
    }
    return UiText.Resource(resource)
}