package com.vkasurinen.knock.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.vkasurinen.knock.chat.database.entities.ChatReadStateEntity

@Dao
interface ChatReadStateDao {

    @Upsert
    suspend fun upsertReadState(state: ChatReadStateEntity)

    @Query("SELECT * FROM chatreadstateentity WHERE chatId = :chatId AND userId = :userId")
    suspend fun getReadState(chatId: String, userId: String): ChatReadStateEntity?

    /**
     * Read states only move forward, so an older event arriving late can't undo a newer one.
     * Ignored for chats that aren't stored locally.
     */
    @Transaction
    suspend fun mergeReadStates(states: List<ChatReadStateEntity>, existingChatIds: Set<String>) {
        states
            .filter { it.chatId in existingChatIds }
            .forEach { state ->
                val current = getReadState(state.chatId, state.userId)
                upsertReadState(
                    state.copy(
                        lastDeliveredAt = maxOfNullable(current?.lastDeliveredAt, state.lastDeliveredAt),
                        lastReadAt = maxOfNullable(current?.lastReadAt, state.lastReadAt)
                    )
                )
            }
    }
}

private fun maxOfNullable(a: Long?, b: Long?): Long? {
    return when {
        a == null -> b
        b == null -> a
        else -> maxOf(a, b)
    }
}
