package com.vkasurinen.knock.chat.data.mappers


import com.vkasurinen.knock.chat.data.dto.ChatParticipantDto
import com.vkasurinen.knock.chat.data.dto.ChatParticipantProfileDto
import com.vkasurinen.knock.chat.database.entities.ChatParticipantEntity
import com.vkasurinen.knock.chat.domain.models.ChatParticipant
import com.vkasurinen.knock.chat.domain.models.UserProfile
import kotlin.time.Instant

fun ChatParticipantDto.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatParticipantEntity.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}

fun ChatParticipant.toEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}
fun ChatParticipantProfileDto.toDomain(): UserProfile {
    return UserProfile(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl,
        memberSince = Instant.parse(memberSince)
    )
}
