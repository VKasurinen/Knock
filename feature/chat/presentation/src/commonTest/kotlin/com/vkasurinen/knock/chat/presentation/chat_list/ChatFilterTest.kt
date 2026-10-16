package com.vkasurinen.knock.chat.presentation.chat_list

import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.core.designsystem.components.avatar.ChatParticipantUi
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatFilterTest {

    private val me = ChatParticipantUi(id = "me", username = "me", initials = "ME")
    private val direct = chat(id = "direct", isGroup = false)
    private val group = chat(id = "group", isGroup = true)
    private val chats = listOf(direct, group)

    @Test
    fun all_showsEveryChat() {
        assertEquals(chats, ChatListState(chats = chats, filter = ChatFilter.ALL).visibleChats)
    }

    @Test
    fun direct_showsOnlyDirectChats() {
        assertEquals(listOf(direct), ChatListState(chats = chats, filter = ChatFilter.DIRECT).visibleChats)
    }

    @Test
    fun groups_showsOnlyGroups() {
        assertEquals(listOf(group), ChatListState(chats = chats, filter = ChatFilter.GROUPS).visibleChats)
    }

    private fun chat(id: String, isGroup: Boolean) = ChatUi(
        id = id,
        localParticipant = me,
        otherParticipants = emptyList(),
        isGroup = isGroup,
        lastMessage = null,
        lastMessageSenderUsername = null
    )
}
