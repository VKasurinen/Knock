package com.vkasurinen.knock.chat.presentation.mappers

import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class ChatMappersTest {

    private val chat = Chat(
        id = "chat-1",
        participants = listOf(
            ChatParticipant(userId = "admin", username = "Admin", profilePictureUrl = null),
            ChatParticipant(userId = "member", username = "Member", profilePictureUrl = null)
        ),
        lastActivityAt = Instant.fromEpochMilliseconds(0),
        lastMessage = null,
        creatorId = "admin"
    )

    @Test
    fun creator_isAdmin() {
        assertTrue(chat.toUi(localParticipantId = "admin").isLocalUserAdmin)
    }

    @Test
    fun otherMember_isNotAdmin() {
        assertFalse(chat.toUi(localParticipantId = "member").isLocalUserAdmin)
    }

    @Test
    fun chatWithoutCreator_hasNoAdmin() {
        assertFalse(chat.copy(creatorId = null).toUi(localParticipantId = "admin").isLocalUserAdmin)
    }

    @Test
    fun removedFlag_isMapped() {
        val chatUi = chat.copy(isLocalUserRemoved = true).toUi(localParticipantId = "member")

        assertTrue(chatUi.isLocalUserRemoved)
    }
}
