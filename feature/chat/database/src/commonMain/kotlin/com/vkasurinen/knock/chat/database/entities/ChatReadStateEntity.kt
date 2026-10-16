package com.vkasurinen.knock.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["chatId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class ChatReadStateEntity(
    val chatId: String,
    val userId: String,
    /** Epoch milliseconds */
    val lastDeliveredAt: Long? = null,
    /** Epoch milliseconds */
    val lastReadAt: Long? = null
)
