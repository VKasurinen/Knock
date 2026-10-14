package com.vkasurinen.knock.core.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseSerializable(
    val code: String? = null,
    val message: String? = null,
    val verificationEmailSent: Boolean? = null
)
