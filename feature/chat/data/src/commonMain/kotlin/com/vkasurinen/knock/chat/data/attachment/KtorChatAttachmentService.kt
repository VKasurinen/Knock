package com.vkasurinen.knock.chat.data.attachment

import com.vkasurinen.knock.chat.data.dto.request.AttachmentUploadUrlsRequest
import com.vkasurinen.knock.chat.data.dto.response.AttachmentUploadUrlResponse
import com.vkasurinen.knock.chat.domain.attachment.ChatAttachmentService
import com.vkasurinen.knock.chat.domain.models.AttachmentUploadUrl
import com.vkasurinen.knock.core.data.networking.post
import com.vkasurinen.knock.core.data.networking.safeCall
import com.vkasurinen.knock.core.domain.util.DataError
import com.vkasurinen.knock.core.domain.util.EmptyResult
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.ByteArrayContent

/**
 * @param uploadHttpClient a plain client for the storage provider's presigned URLs. It must not
 * be the API client, which would attach the user's JWT and API key to the upload requests.
 */
class KtorChatAttachmentService(
    private val httpClient: HttpClient,
    private val uploadHttpClient: HttpClient
): ChatAttachmentService {

    override suspend fun getUploadUrls(
        chatId: String,
        files: List<ChatAttachmentService.AttachmentFile>
    ): Result<List<AttachmentUploadUrl>, DataError.Remote> {
        return httpClient.post<AttachmentUploadUrlsRequest, List<AttachmentUploadUrlResponse>>(
            route = "/chat/$chatId/attachments/upload-urls",
            body = AttachmentUploadUrlsRequest(
                attachments = files.map {
                    AttachmentUploadUrlsRequest.Attachment(
                        mimeType = it.mimeType,
                        fileName = it.fileName
                    )
                }
            )
        ).map { response ->
            response.map {
                AttachmentUploadUrl(
                    uploadUrl = it.uploadUrl,
                    publicUrl = it.publicUrl,
                    headers = it.headers
                )
            }
        }
    }

    override suspend fun upload(
        uploadUrl: AttachmentUploadUrl,
        bytes: ByteArray,
        mimeType: String,
        onProgress: (bytesSent: Long) -> Unit
    ): EmptyResult<DataError.Remote> {
        return safeCall<Unit> {
            uploadHttpClient.put {
                url(uploadUrl.uploadUrl)
                uploadUrl.headers
                    // Set through the body below, Ktor doesn't allow it as a plain header
                    .filterKeys { !it.equals(HttpHeaders.ContentType, ignoreCase = true) }
                    .forEach { (key, value) -> header(key, value) }
                setBody(ByteArrayContent(bytes, ContentType.parse(mimeType)))
                onUpload { bytesSentTotal, _ ->
                    onProgress(bytesSentTotal)
                }
            }
        }
    }
}
