package com.vkasurinen.knock.chat.data.mappers

import com.vkasurinen.knock.chat.data.dto.ChatMessageAttachmentDto
import com.vkasurinen.knock.chat.data.dto.websocket.IncomingWebSocketDto
import com.vkasurinen.knock.chat.domain.models.AttachmentType
import com.vkasurinen.knock.chat.domain.models.ChatMessageDeliveryStatus
import com.vkasurinen.knock.chat.domain.models.OutgoingAttachment
import com.vkasurinen.knock.chat.domain.models.OutgoingNewMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AttachmentMappersTest {

    private val outgoingAttachment = OutgoingAttachment(
        type = AttachmentType.IMAGE,
        localPath = "/files/attachments/a.jpg",
        fileName = "a.jpg",
        mimeType = "image/jpeg",
        sizeBytes = 1234,
        width = 800,
        height = 600
    )

    private val outgoingMessage = OutgoingNewMessage(
        chatId = "chat-1",
        messageId = "message-1",
        content = "",
        attachments = listOf(outgoingAttachment)
    )

    @Test
    fun outgoingMessage_storesLocalAttachmentsWithoutUrl() {
        val entity = outgoingMessage.toEntity(
            senderId = "me",
            deliveryStatus = ChatMessageDeliveryStatus.SENDING
        )

        val attachment = entity.toDomain().attachments.single()
        assertEquals(AttachmentType.IMAGE, attachment.type)
        assertEquals("/files/attachments/a.jpg", attachment.localPath)
        assertNull(attachment.url)
        assertEquals(800, attachment.width)
        assertEquals(600, attachment.height)
    }

    @Test
    fun webSocketDto_containsUploadedUrls() {
        val entity = outgoingMessage.toEntity("me", ChatMessageDeliveryStatus.SENDING)
        val uploaded = entity.copy(
            attachments = encodeAttachments(
                decodeAttachments(entity.attachments).map { it.copy(url = "https://storage/a.jpg") }
            )
        )

        val dto = uploaded.toWebSocketDto()

        val attachment = dto.attachments.single()
        assertEquals("https://storage/a.jpg", attachment.url)
        assertEquals("image/jpeg", attachment.mimeType)
        assertEquals(1234, attachment.sizeBytes)
    }

    @Test
    fun webSocketDto_failsForAttachmentsThatAreNotUploaded() {
        val entity = outgoingMessage.toEntity("me", ChatMessageDeliveryStatus.SENDING)

        assertFailsWith<IllegalArgumentException> {
            entity.toWebSocketDto()
        }
    }

    @Test
    fun incomingMessage_keepsLocalPathsByPosition() {
        val dto = IncomingWebSocketDto.NewMessageDto(
            id = "message-1",
            chatId = "chat-1",
            content = "",
            senderId = "me",
            createdAt = "2026-10-12T12:00:00Z",
            attachments = listOf(
                serverAttachment(id = "1", url = "https://storage/a.jpg"),
                serverAttachment(id = "2", url = "https://storage/b.jpg")
            )
        )

        val attachments = dto.toEntity(localPaths = listOf("/local/a.jpg")).toDomain().attachments

        assertEquals("https://storage/a.jpg", attachments[0].url)
        assertEquals("/local/a.jpg", attachments[0].localPath)
        assertEquals("1", attachments[0].id)
        assertEquals("https://storage/b.jpg", attachments[1].url)
        assertNull(attachments[1].localPath)
    }

    @Test
    fun unknownAttachmentType_fallsBackToImage() {
        val attachment = serverAttachment(id = "1", url = "https://storage/a.jpg")
            .copy(type = "VIDEO")
            .toDomain()

        assertEquals(AttachmentType.IMAGE, attachment.type)
    }

    @Test
    fun emptyAttachmentColumn_decodesToEmptyList() {
        assertEquals(emptyList(), decodeAttachments(""))
        assertEquals(emptyList(), decodeAttachments("[]"))
    }

    private fun serverAttachment(id: String, url: String) = ChatMessageAttachmentDto(
        id = id,
        type = "IMAGE",
        url = url,
        fileName = "$id.jpg",
        mimeType = "image/jpeg",
        sizeBytes = 100
    )
}
