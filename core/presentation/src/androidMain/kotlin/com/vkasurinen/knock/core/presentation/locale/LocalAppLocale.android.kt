package com.vkasurinen.knock.core.presentation.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null

    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Suppress("DEPRECATION")
    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        if(default == null) {
            default = Locale.getDefault()
        }
        val newLocale = when(value) {
            null -> default!!
            else -> Locale.forLanguageTag(value)
        }
        Locale.setDefault(newLocale)
        val newConfiguration = Configuration(configuration).apply { setLocale(newLocale) }
        val resources = LocalContext.current.resources
        resources.updateConfiguration(newConfiguration, resources.displayMetrics)
        return LocalConfiguration.provides(newConfiguration)
    }
}
