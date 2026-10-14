package com.vkasurinen.knock.chat.data.mappers

import com.vkasurinen.knock.chat.data.dto.response.ProfilePictureUploadUrlsResponse
import com.vkasurinen.knock.chat.domain.models.ProfilePictureUploadUrls

fun ProfilePictureUploadUrlsResponse.toDomain(): ProfilePictureUploadUrls {
    return ProfilePictureUploadUrls(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers
    )
}