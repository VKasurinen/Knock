package com.vkasurinen.knock.core.presentation.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import com.vkasurinen.knock.core.domain.preferences.AppLanguage

/**
 * Applies the user's [language] to everything inside [content]. Changing the language
 * recomposes the content immediately, no restart needed. Keep state that must survive the
 * switch (e.g. the NavController) outside of [content].
 */
@Composable
fun AppLocaleEnvironment(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppLocale provides language.languageTag) {
        key(language) {
            content()
        }
    }
}
