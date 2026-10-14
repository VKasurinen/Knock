package com.vkasurinen.knock.chat.database

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object KnockChatDatabaseConstructor: RoomDatabaseConstructor<KnockChatDatabase> {
    override fun initialize(): KnockChatDatabase
}