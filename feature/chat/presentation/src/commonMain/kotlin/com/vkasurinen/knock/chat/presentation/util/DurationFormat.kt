package com.vkasurinen.knock.chat.presentation.util

/**
 * Formats a duration as m:ss, e.g. 1:05.
 */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs.coerceAtLeast(0L) + 500L) / 1000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
