package com.vkasurinen.knock.chat.domain.error

import com.vkasurinen.knock.core.domain.util.Error

enum class ConnectionError: Error {
    NOT_CONNECTED,
    MESSAGE_SEND_FAILED
}