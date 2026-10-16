package com.vkasurinen.knock.chat.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RenameChatRequest(
    val name: String?
)
