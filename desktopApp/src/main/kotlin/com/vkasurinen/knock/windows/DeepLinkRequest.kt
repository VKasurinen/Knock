package com.vkasurinen.knock.windows

import java.util.UUID

/**
 * Brings the window with [windowId] to the front and opens [uri] in it.
 */
data class DeepLinkRequest(
    val windowId: String,
    val uri: String,
    // Opening the same link twice is two requests
    val id: String = UUID.randomUUID().toString()
)
