package com.vkasurinen.knock.core.domain.preferences

/**
 * Language of the app UI. [SYSTEM] follows the device language (English unless it's Finnish).
 */
enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    FINNISH("fi"),
    ENGLISH("en")
}
