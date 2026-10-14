package com.vkasurinen.knock.core.domain.logging

interface KnockLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}