package com.vkasurinen.knock.chat.domain.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Plays one voice message at a time. Playback keeps going while the app is in the background.
 */
interface AudioPlayer {
    val playbackState: StateFlow<PlaybackState>

    /**
     * Plays [source] (a URL or a file:// path). Resumes instead of restarting when [id] is
     * already the current item, unless [startPositionMs] is given.
     */
    fun play(id: String, source: String, durationMs: Long?, startPositionMs: Long? = null)

    fun pause()

    fun seekTo(positionMs: Long)
}

data class PlaybackState(
    /** Id of the voice message that is playing or paused, null when nothing is loaded */
    val id: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)
