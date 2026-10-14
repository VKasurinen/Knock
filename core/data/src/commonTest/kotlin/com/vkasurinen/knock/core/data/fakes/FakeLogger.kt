package com.vkasurinen.knock.core.data.fakes

import com.vkasurinen.knock.core.domain.logging.KnockLogger

object FakeLogger: KnockLogger {
    override fun debug(message: String) = Unit
    override fun info(message: String) = Unit
    override fun warn(message: String) = Unit
    override fun error(message: String, throwable: Throwable?) = Unit
}
