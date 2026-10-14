package com.vkasurinen.knock.chat.presentation.model

data class RecordingUi(
    val elapsedMs: Long = 0L,
    /** Most recent input levels (0..1), oldest first */
    val amplitudes: List<Float> = emptyList()
)
