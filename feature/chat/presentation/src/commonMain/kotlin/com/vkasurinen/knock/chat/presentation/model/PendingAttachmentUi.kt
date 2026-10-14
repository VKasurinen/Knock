package com.vkasurinen.knock.chat.presentation.model

import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment

/**
 * An attachment in the composer that hasn't been sent yet.
 */
data class PendingAttachmentUi(
    val id: String,
    val fileName: String,
    val state: State
) {
    sealed interface State {
        data object Processing: State
        data class Ready(val attachment: OutgoingAttachment): State
        data object Failed: State
    }
}
