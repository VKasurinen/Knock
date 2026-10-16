package com.vkasurinen.knock.chat.presentation.user_profile

import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import com.vkasurinen.knock.core.presentation.util.UiText
import kotlin.time.Instant

data class UserProfileState(
    val participant: ChatParticipantUi? = null,
    val memberSince: Instant? = null,
    val commonGroups: List<ChatUi> = emptyList(),
    val isOpeningChat: Boolean = false,
    /** The profile picture is open full screen */
    val isPictureOpen: Boolean = false,
    val error: UiText? = null
)
