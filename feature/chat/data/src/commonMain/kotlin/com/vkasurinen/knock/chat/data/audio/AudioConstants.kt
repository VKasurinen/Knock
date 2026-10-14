package com.vkasurinen.knock.chat.data.audio

import kotlin.time.Clock

/** The server rejects voice messages longer than 10 minutes */
internal const val MAX_RECORDING_DURATION_MS = 10 * 60 * 1000L
internal const val VOICE_MESSAGE_MIME_TYPE = "audio/mp4"
internal const val VOICE_MESSAGE_EXTENSION = "m4a"
internal const val PLAYBACK_POLL_INTERVAL_MS = 100L

internal fun voiceMessageFileName(): String {
    return "voice_${Clock.System.now().toEpochMilliseconds()}.$VOICE_MESSAGE_EXTENSION"
}
