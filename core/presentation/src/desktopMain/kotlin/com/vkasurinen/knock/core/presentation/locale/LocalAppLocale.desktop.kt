package com.vkasurinen.knock.core.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalAppLocale = staticCompositionLocalOf { Locale.getDefault().toString() }

    actual val current: String
        @Composable get() = LocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if(default == null) {
            default = Locale.getDefault()
        }
        val newLocale = when(value) {
            null -> default!!
            else -> Locale.forLanguageTag(value)
        }
        Locale.setDefault(newLocale)
        return LocalAppLocale.provides(newLocale.toString())
    }
}
