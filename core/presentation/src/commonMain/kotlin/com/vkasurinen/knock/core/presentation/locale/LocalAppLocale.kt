package com.vkasurinen.knock.core.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Overrides the locale the app (and Compose resources) use at runtime.
 * Providing `null` restores the device locale.
 */
expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
