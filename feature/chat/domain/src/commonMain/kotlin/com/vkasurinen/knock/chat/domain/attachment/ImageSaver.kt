package com.vkasurinen.knock.chat.domain.attachment

import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult

interface ImageSaver {
    /**
     * Saves the image to the device's photo gallery.
     */
    suspend fun saveToGallery(attachment: MessageAttachment): EmptyResult<DataError>
}
