package com.vkasurinen.knock.chat.domain.models

import kotlin.time.Instant

data class UserProfile(
    val userId: String,
    val username: String,
    val profilePictureUrl: String?,
    val memberSince: Instant
)
