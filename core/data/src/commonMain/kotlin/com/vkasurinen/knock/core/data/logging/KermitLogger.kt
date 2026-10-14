package com.vkasurinen.knock.core.data.logging

import co.touchlab.kermit.Logger
import com.vkasurinen.knock.core.domain.logging.KnockLogger

object KermitLogger: KnockLogger {

    override fun debug(message: String) {
        Logger.d(message)
    }

    override fun info(message: String) {
        Logger.i(message)
    }

    override fun warn(message: String) {
        Logger.w(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Logger.e(message, throwable)
    }
}