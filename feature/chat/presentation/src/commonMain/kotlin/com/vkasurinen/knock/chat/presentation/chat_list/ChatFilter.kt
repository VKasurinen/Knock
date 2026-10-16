package com.vkasurinen.knock.chat.presentation.chat_list

import com.vkasurinen.knock.chat.presentation.model.ChatUi

enum class ChatFilter {
    ALL,
    DIRECT,
    GROUPS;

    fun matches(chat: ChatUi): Boolean {
        return when(this) {
            ALL -> true
            DIRECT -> !chat.isGroup
            GROUPS -> chat.isGroup
        }
    }
}
