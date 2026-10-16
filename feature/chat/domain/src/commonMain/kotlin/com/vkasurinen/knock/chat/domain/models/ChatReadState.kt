package com.vkasurinen.knock.chat.domain.models

import kotlin.time.Instant

/**
 * How far a participant has received and read a chat: the time of the newest message from someone
 * else at that point. A message is delivered or read once every other participant has reached it.
 */
data class ChatReadState(
    val userId: String,
    val lastDeliveredAt: Instant?,
    val lastReadAt: Instant?
)
