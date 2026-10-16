package com.vkasurinen.knock.chat.presentation.mappers

import com.vkasurinen.knock.chat.domain.models.Chat
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.MessageWithSender
import kotlin.time.Instant
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.chat.presentation.util.DateUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


/**
 * How far every other participant of a chat has received and read it, which gives the ticks of the
 * local user's messages. Null when someone hasn't received or read anything yet.
 */
data class ReadReceipts(
    val deliveredUpTo: Instant?,
    val readUpTo: Instant?
)

fun Chat.readReceipts(localUserId: String): ReadReceipts {
    val otherIds = participants.map { it.userId }.filter { it != localUserId }
    if(otherIds.isEmpty()) {
        return ReadReceipts(deliveredUpTo = null, readUpTo = null)
    }
    val states = otherIds.map { userId -> readStates.find { it.userId == userId } }
    fun minOrNull(values: List<Instant?>): Instant? {
        return if(values.any { it == null }) null else values.filterNotNull().minOrNull()
    }
    return ReadReceipts(
        deliveredUpTo = minOrNull(states.map { it?.lastDeliveredAt }),
        readUpTo = minOrNull(states.map { it?.lastReadAt })
    )
}

fun List<MessageWithSender>.toUiList(
    localUserId: String,
    readReceipts: ReadReceipts = ReadReceipts(deliveredUpTo = null, readUpTo = null)
): List<MessageUi> {
    return this
        .sortedByDescending { it.message.createdAt }
        .groupBy {
            it.message.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        .flatMap { (date, messages) ->
            messages.map { it.toUi(localUserId, readReceipts) } + MessageUi.DateSeparator(
                id = date.toString(),
                date = DateUtils.formatDateSeparator(date)
            )
        }
}

fun MessageWithSender.toUi(
    localUserId: String,
    readReceipts: ReadReceipts = ReadReceipts(deliveredUpTo = null, readUpTo = null)
): MessageUi {
    val isFromLocalUser = this.sender.userId == localUserId
    return if(isFromLocalUser) {
        MessageUi.LocalUserMessage(
            id = message.id,
            content = message.content,
            deliveryStatus = displayedStatus(readReceipts),
            formattedSentTime = DateUtils.formatBubbleTime(instant = message.createdAt),
            attachments = message.attachments
        )
    } else {
        MessageUi.OtherUserMessage(
            id = message.id,
            content = message.content,
            formattedSentTime = DateUtils.formatBubbleTime(instant = message.createdAt),
            sender = sender.toUi(),
            attachments = message.attachments
        )
    }
}

private fun MessageWithSender.displayedStatus(readReceipts: ReadReceipts): ChatMessageDeliveryStatus {
    if(message.deliveryStatus != ChatMessageDeliveryStatus.SENT) {
        return message.deliveryStatus
    }
    val sentAt = message.createdAt
    return when {
        readReceipts.readUpTo != null && readReceipts.readUpTo >= sentAt -> ChatMessageDeliveryStatus.READ
        readReceipts.deliveredUpTo != null && readReceipts.deliveredUpTo >= sentAt -> ChatMessageDeliveryStatus.DELIVERED
        else -> ChatMessageDeliveryStatus.SENT
    }
}
