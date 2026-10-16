package com.vkasurinen.knock.chat.domain.models

enum class ChatType {
    /** A private conversation between two users. Participants can't be added. */
    DIRECT,
    /** A chat with any number of participants and an optional name. */
    GROUP
}
