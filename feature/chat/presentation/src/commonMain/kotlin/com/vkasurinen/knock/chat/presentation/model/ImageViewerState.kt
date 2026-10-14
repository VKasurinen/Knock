package com.vkasurinen.knock.chat.presentation.model

import com.vkasurinen.knock.chat.domain.models.MessageAttachment

data class ImageViewerState(
    val images: List<MessageAttachment>,
    val initialIndex: Int
)
