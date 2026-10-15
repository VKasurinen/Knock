package com.vkasurinen.knock.core.data.networking

import com.vkasurinen.knock.core.data.BuildKonfig

object UrlConstants {
    val BASE_URL_HTTP = "${BuildKonfig.BASE_URL}/api"
    val BASE_URL_WS = "${BuildKonfig.BASE_URL.replaceFirst("http", "ws")}/ws"
}