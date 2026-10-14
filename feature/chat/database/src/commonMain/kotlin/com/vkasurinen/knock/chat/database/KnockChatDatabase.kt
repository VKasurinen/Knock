package com.vkasurinen.knock.chat.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import com.vkasurinen.knock.chat.database.dao.ChatDao
import com.vkasurinen.knock.chat.database.dao.ChatMessageDao
import com.vkasurinen.knock.chat.database.dao.ChatParticipantDao
import com.vkasurinen.knock.chat.database.dao.ChatParticipantsCrossRefDao
import com.vkasurinen.knock.chat.database.entities.ChatEntity
import com.vkasurinen.knock.chat.database.entities.ChatMessageEntity
import com.vkasurinen.knock.chat.database.entities.ChatParticipantCrossRef
import com.vkasurinen.knock.chat.database.entities.ChatParticipantEntity
import com.vkasurinen.knock.chat.database.view.LastMessageView

@Database(
    entities = [
        ChatEntity::class,
        ChatParticipantEntity::class,
        ChatMessageEntity::class,
        ChatParticipantCrossRef::class,
    ],
    views = [
        LastMessageView::class
    ],
    version = 3,
)
@ConstructedBy(KnockChatDatabaseConstructor::class)
abstract class KnockChatDatabase: RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val chatParticipantDao: ChatParticipantDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatParticipantsCrossRefDao: ChatParticipantsCrossRefDao

    companion object {
        const val DB_NAME = "knock.db"
    }
}