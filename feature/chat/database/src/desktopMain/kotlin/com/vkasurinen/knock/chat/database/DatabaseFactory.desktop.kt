package com.vkasurinen.knock.chat.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.vkasurinen.knock.core.data.util.appDataDirectory
import java.io.File

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<KnockChatDatabase> {
        val directory = appDataDirectory

        if(!directory.exists()) {
            directory.mkdirs()
        }

        val dbFile = File(directory, KnockChatDatabase.DB_NAME)
        return Room.databaseBuilder(dbFile.absolutePath)
    }
}