package com.vkasurinen.knock.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vkasurinen.knock.chat.database.entities.ChatParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatParticipantDao {

    @Upsert
    suspend fun upsertParticipant(participant: ChatParticipantEntity)

    @Upsert
    suspend fun upsertParticipants(participants: List<ChatParticipantEntity>)

    @Query("""
        UPDATE chatparticipantentity
        SET profilePictureUrl = :newUrl
        WHERE userId = :userId
    """)
    suspend fun updateProfilePictureUrl(userId: String, newUrl: String?)

    @Query("SELECT * FROM chatparticipantentity WHERE userId = :userId")
    suspend fun getParticipantById(userId: String): ChatParticipantEntity?

    @Query("SELECT * FROM chatparticipantentity WHERE userId = :userId")
    fun observeParticipantById(userId: String): Flow<ChatParticipantEntity?>

    @Query("SELECT * FROM chatparticipantentity")
    suspend fun getAllParticipants(): List<ChatParticipantEntity>
}