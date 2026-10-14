package com.vkasurinen.knock.windows

import java.util.UUID

data class WindowState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Knock",
    val isFocused: Boolean = false
)
