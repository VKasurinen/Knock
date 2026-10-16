@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)

package com.vkasurinen.knock.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import knock.feature.chat.presentation.generated.resources.Res
import knock.feature.chat.presentation.generated.resources.image_saved
import knock.feature.chat.presentation.generated.resources.max_attachments_reached
import knock.feature.chat.presentation.generated.resources.member_was_removed_from_chat
import knock.feature.chat.presentation.generated.resources.recording_failed
import knock.feature.chat.presentation.generated.resources.today
import knock.feature.chat.presentation.generated.resources.voice_message_too_short
import knock.feature.chat.presentation.generated.resources.x_was_removed_from_chat
import knock.feature.chat.presentation.generated.resources.you_were_removed_from_chat
import com.vkasurinen.knock.chat.domain.attachment.ImageCompressor
import com.vkasurinen.knock.chat.domain.attachment.ImageSaver
import com.vkasurinen.knock.chat.domain.audio.AudioPlayer
import com.vkasurinen.knock.chat.domain.audio.AudioRecorder
import com.vkasurinen.knock.chat.domain.chat.ChatConnectionClient
import com.vkasurinen.knock.chat.domain.chat.ChatRepository
import com.vkasurinen.knock.chat.domain.notification.PushNotificationService
import com.vkasurinen.knock.chat.domain.message.MessageRepository
import com.vkasurinen.knock.chat.domain.models.ChatEvent
import com.vkasurinen.knock.chat.domain.models.ChatMessage
import com.vkasurinen.knock.chat.domain.models.ConnectionState
import com.vkasurinen.knock.chat.domain.models.MessageAttachment
import com.vkasurinen.knock.chat.domain.models.OutgoingNewMessage
import com.vkasurinen.knock.chat.domain.typing.TypingRepository
import com.vkasurinen.knock.chat.presentation.mappers.toUi
import com.vkasurinen.knock.chat.presentation.mappers.readReceipts
import com.vkasurinen.knock.chat.presentation.mappers.toUiList
import com.vkasurinen.knock.chat.presentation.model.ChatUi
import com.vkasurinen.knock.chat.presentation.model.ImageViewerState
import com.vkasurinen.knock.chat.presentation.model.MessageUi
import com.vkasurinen.knock.chat.presentation.model.PendingAttachmentUi
import com.vkasurinen.knock.chat.presentation.model.RecordingUi
import com.vkasurinen.knock.chat.presentation.profile.mediapicker.PickedImageData
import com.vkasurinen.knock.chat.presentation.util.mediaSource
import com.vkasurinen.knock.chat.presentation.util.typingIndicatorText
import com.vkasurinen.knock.core.domain.auth.SessionStorage
import com.vkasurinen.knock.core.domain.util.DataErrorException
import com.vkasurinen.knock.core.domain.util.Paginator
import com.vkasurinen.knock.core.domain.util.Result
import com.vkasurinen.knock.core.domain.util.onFailure
import com.vkasurinen.knock.core.domain.util.onSuccess
import com.vkasurinen.knock.core.presentation.util.UiText
import com.vkasurinen.knock.core.presentation.util.toUiText
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val sessionStorage: SessionStorage,
    private val messageRepository: MessageRepository,
    private val connectionClient: ChatConnectionClient,
    private val typingRepository: TypingRepository,
    private val imageCompressor: ImageCompressor,
    private val imageSaver: ImageSaver,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val pushNotificationService: PushNotificationService
) : ViewModel() {

    // Both update every 100ms, so they are kept out of the main state to avoid
    // rebuilding the message list on every tick
    private val _recording = MutableStateFlow<RecordingUi?>(null)
    val recording = _recording.asStateFlow()
    val playbackState = audioPlayer.playbackState
    private var recordingJob: Job? = null


    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _chatId = MutableStateFlow<String?>(null)

    private var hasLoadedInitialData = false

    private var currentPaginator: Paginator<String?, ChatMessage>? = null

    private val chatInfoFlow = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository.getChatInfoById(chatId)
            } else emptyFlow()
        }

    private val _state = MutableStateFlow(ChatDetailState())

    /** Whether the app is in the foreground with this chat open */
    private val _isScreenVisible = MutableStateFlow(false)

    private val canSendMessage = combine(
        snapshotFlow { _state.value.messageTextFieldState.text.toString() }.map { it.isBlank() },
        _state.map { it.pendingAttachments }.distinctUntilChanged(),
        connectionClient.connectionState
    ) { isMessageBlank, pendingAttachments, connectionState ->
        val hasReadyAttachments = pendingAttachments.any { it.state is PendingAttachmentUi.State.Ready }
        val isProcessing = pendingAttachments.any { it.state is PendingAttachmentUi.State.Processing }

        (!isMessageBlank || hasReadyAttachments) &&
                !isProcessing &&
                connectionState == ConnectionState.CONNECTED
    }


    /**
     * The chat and its messages mapped for the UI. Only rebuilt when the data changes, and off
     * the main thread, as mapping every message is too slow to do during the navigation animation.
     */
    private val chatContentFlow = combine(
        chatInfoFlow,
        sessionStorage.observeAuthInfo(),
        messageRepository.observeUploadProgress()
    ) { chatInfo, authInfo, uploadProgress ->
        if (authInfo == null) {
            return@combine null
        }
        ChatContent(
            chatUi = chatInfo.chat.toUi(authInfo.user.id),
            messages = chatInfo.messages
                .toUiList(
                    localUserId = authInfo.user.id,
                    readReceipts = chatInfo.chat.readReceipts(authInfo.user.id)
                )
                .withUploadProgress(uploadProgress)
        )
    }
        // The database emits the chat again after every sync, mostly with nothing changed
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    private val stateWithMessages = combine(
        _state,
        chatContentFlow
    ) { currentState, chatContent ->
        if (chatContent == null) {
            return@combine ChatDetailState()
        }

        val typingUsernames = currentState.typingUserIds.mapNotNull { userId ->
            chatContent.chatUi.otherParticipants.find { it.id == userId }?.username
        }

        currentState.copy(
            chatUi = chatContent.chatUi,
            messages = chatContent.messages,
            typingText = typingIndicatorText(typingUsernames)
        )
    }

    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                stateWithMessages
            } else {
                _state
            }
        }
        .onStart {
            if (!hasLoadedInitialData) {
                observeSelectedChat()
                observeConnectionState()
                observeCanSendMessage()
                observeTypingUsers()
                observeLocalUserTyping()
                observeChatEvents()
                observeReadMessages()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatDetailState()
        )

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is ChatDetailAction.OnSelectChat -> switchChat(action.chatId)
            ChatDetailAction.OnScreenResumed -> {
                _isScreenVisible.value = true
                _chatId.value?.let(pushNotificationService::clearChatNotifications)
            }
            ChatDetailAction.OnScreenPaused -> _isScreenVisible.value = false
            ChatDetailAction.OnChatOptionsClick -> onChatOptionsClick()
            is ChatDetailAction.OnDeleteMessageClick -> deleteMessage(action.message)
            ChatDetailAction.OnDismissChatOptions -> onDismissChatOptions()
            ChatDetailAction.OnDismissMessageMenu -> onDismissMessageMenu()
            ChatDetailAction.OnLeaveChatClick -> onLeaveChatClick()
            ChatDetailAction.OnConfirmLeaveChatClick -> leaveChat()
            ChatDetailAction.OnDismissLeaveChatConfirmation -> dismissLeaveChatConfirmation()
            is ChatDetailAction.OnMessageLongClick -> onMessageLongClick(action.message)
            is ChatDetailAction.OnRetryClick -> retryMessage(action.message)
            ChatDetailAction.OnScrollToTop -> onScrollToTop()
            ChatDetailAction.OnSendMessageClick -> sendMessage()
            ChatDetailAction.OnRetryPaginationClick -> retryPagination()
            ChatDetailAction.OnHideBanner -> hideBanner()
            is ChatDetailAction.OnTopVisibleIndexChanged -> updateBanner(action.topVisibleIndex)
            is ChatDetailAction.OnFirstVisibleIndexChanged -> updateNearBottom(action.index)
            is ChatDetailAction.OnImagesPicked -> addPendingImages(action.images)
            is ChatDetailAction.OnRemovePendingAttachment -> removePendingAttachment(action.id)
            is ChatDetailAction.OnImageClick -> openImageViewer(action.images, action.index)
            ChatDetailAction.OnDismissImageViewer -> closeImageViewer()
            is ChatDetailAction.OnSaveImageClick -> saveImage(action.image)
            ChatDetailAction.OnStartRecording -> startRecording()
            ChatDetailAction.OnCancelRecordingClick -> cancelRecording()
            ChatDetailAction.OnSendRecordingClick -> sendRecording()
            is ChatDetailAction.OnVoiceMessagePlayPauseClick -> togglePlayback(action.messageId, action.voiceMessage)
            is ChatDetailAction.OnVoiceMessageSeek -> seekVoiceMessage(action.messageId, action.voiceMessage, action.positionMs)
            else -> Unit
        }
    }

    private fun updateNearBottom(firstVisibleIndex: Int) {
        _state.update { it.copy(
            isNearBottom = firstVisibleIndex <= 3
        ) }
    }

    private fun updateBanner(topVisibleIndex: Int) {
        val visibleDate = calculateBannerDateFromIndex(
            messages = state.value.messages,
            index = topVisibleIndex
        )

        _state.update { it.copy(
            bannerState = BannerState(
                formattedDate = visibleDate,
                isVisible = visibleDate != null
            )
        ) }
    }

    private fun calculateBannerDateFromIndex(
        messages: List<MessageUi>,
        index: Int
    ): UiText? {
        if(messages.isEmpty() || index < 0 || index >= messages.size) {
            return null
        }

        val nearestDateSeparator = (index until messages.size)
            .asSequence()
            .mapNotNull { index ->
                val item = messages.getOrNull(index)
                if(item is MessageUi.DateSeparator) item.date else null
            }
            .firstOrNull()

        return when(nearestDateSeparator) {
            is UiText.Resource -> {
                if(nearestDateSeparator.id == Res.string.today) null else nearestDateSeparator
            }
            else -> nearestDateSeparator
        }
    }

    private fun hideBanner() {
        _state.update {
            it.copy(
                bannerState = it.bannerState.copy(
                    isVisible = false
                )
            )
        }
    }

    private fun retryPagination() = loadNextItems()

    private fun onScrollToTop() = loadNextItems()

    private fun loadNextItems() {
        viewModelScope.launch {
            currentPaginator?.loadNextItems()
        }
    }

    private fun onDismissMessageMenu() {
        _state.update {
            it.copy(
                messageWithOpenMenu = null
            )
        }
    }

    private fun onMessageLongClick(message: MessageUi.LocalUserMessage) {
        _state.update {
            it.copy(
                messageWithOpenMenu = message
            )
        }
    }

    private fun deleteMessage(message: MessageUi.LocalUserMessage) {
        viewModelScope.launch {
            messageRepository
                .deleteMessage(message.id)
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun retryMessage(message: MessageUi.LocalUserMessage) {
        viewModelScope.launch {
            messageRepository
                .retryMessage(message.id)
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun sendMessage() {
        val currentChatId = _chatId.value
        val content = state.value.messageTextFieldState.text.toString().trim()
        val attachments = _state.value.pendingAttachments.mapNotNull {
            (it.state as? PendingAttachmentUi.State.Ready)?.attachment
        }
        if ((content.isBlank() && attachments.isEmpty()) || currentChatId == null) {
            return
        }

        val message = OutgoingNewMessage(
            chatId = currentChatId,
            messageId = Uuid.random().toString(),
            content = content,
            attachments = attachments
        )

        // The message is stored locally right away and can be retried from the list if it
        // fails, so the composer doesn't need to wait for the upload
        state.value.messageTextFieldState.clearText()
        _state.update { it.copy(pendingAttachments = emptyList()) }

        viewModelScope.launch {
            messageRepository
                .sendMessage(message)
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun addPendingImages(images: List<PickedImageData>) {
        val remainingSlots = MAX_ATTACHMENTS - _state.value.pendingAttachments.size
        if (images.size > remainingSlots) {
            viewModelScope.launch {
                eventChannel.send(
                    ChatDetailEvent.OnInfo(
                        UiText.Resource(Res.string.max_attachments_reached, arrayOf(MAX_ATTACHMENTS))
                    )
                )
            }
        }

        images.take(remainingSlots.coerceAtLeast(0)).forEach { image ->
            val pending = PendingAttachmentUi(
                id = Uuid.random().toString(),
                fileName = image.fileName ?: "Image",
                state = PendingAttachmentUi.State.Processing
            )
            _state.update { it.copy(pendingAttachments = it.pendingAttachments + pending) }

            viewModelScope.launch {
                val newState = when (val result = imageCompressor.compress(image.bytes, image.fileName)) {
                    is Result.Success -> PendingAttachmentUi.State.Ready(result.data)
                    is Result.Failure -> PendingAttachmentUi.State.Failed
                }
                _state.update {
                    it.copy(
                        pendingAttachments = it.pendingAttachments.map { attachment ->
                            if (attachment.id == pending.id) attachment.copy(state = newState) else attachment
                        }
                    )
                }
            }
        }
    }

    private fun startRecording() {
        if (_recording.value != null) {
            return
        }
        audioPlayer.pause()

        audioRecorder
            .start()
            .onFailure {
                viewModelScope.launch {
                    eventChannel.send(ChatDetailEvent.OnError(UiText.Resource(Res.string.recording_failed)))
                }
                return
            }

        _recording.value = RecordingUi()
        recordingJob = viewModelScope.launch {
            val startMark = TimeSource.Monotonic.markNow()
            while (isActive) {
                delay(AMPLITUDE_POLL_INTERVAL_MS)
                val elapsedMs = startMark.elapsedNow().inWholeMilliseconds
                _recording.update { recording ->
                    recording?.copy(
                        elapsedMs = elapsedMs,
                        amplitudes = (recording.amplitudes + audioRecorder.currentAmplitude())
                            .takeLast(MAX_WAVEFORM_BARS)
                    )
                }
                if (elapsedMs >= MAX_RECORDING_DURATION_MS) {
                    sendRecording()
                }
            }
        }
    }

    private fun cancelRecording() {
        if (_recording.value == null) {
            return
        }
        recordingJob?.cancel()
        _recording.value = null
        audioRecorder.cancel()
    }

    private fun sendRecording() {
        val chatId = _chatId.value
        if (_recording.value == null) {
            return
        }
        recordingJob?.cancel()
        _recording.value = null

        viewModelScope.launch {
            val voiceMessage = when (val result = audioRecorder.stop()) {
                is Result.Success -> result.data
                is Result.Failure -> {
                    eventChannel.send(ChatDetailEvent.OnError(UiText.Resource(Res.string.recording_failed)))
                    return@launch
                }
            }
            if ((voiceMessage.durationMs ?: 0L) < MIN_RECORDING_DURATION_MS) {
                eventChannel.send(ChatDetailEvent.OnInfo(UiText.Resource(Res.string.voice_message_too_short)))
                return@launch
            }
            if (chatId == null) {
                return@launch
            }

            messageRepository
                .sendMessage(
                    OutgoingNewMessage(
                        chatId = chatId,
                        messageId = Uuid.random().toString(),
                        content = "",
                        attachments = listOf(voiceMessage)
                    )
                )
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun togglePlayback(messageId: String, voiceMessage: MessageAttachment) {
        val playback = playbackState.value
        if (playback.id == messageId && playback.isPlaying) {
            audioPlayer.pause()
            return
        }
        val source = voiceMessage.mediaSource ?: return
        audioPlayer.play(
            id = messageId,
            source = source,
            durationMs = voiceMessage.durationMs
        )
    }

    private fun seekVoiceMessage(messageId: String, voiceMessage: MessageAttachment, positionMs: Long) {
        if (playbackState.value.id == messageId) {
            audioPlayer.seekTo(positionMs)
            return
        }
        val source = voiceMessage.mediaSource ?: return
        audioPlayer.play(
            id = messageId,
            source = source,
            durationMs = voiceMessage.durationMs,
            startPositionMs = positionMs
        )
    }

    override fun onCleared() {
        super.onCleared()
        cancelRecording()
        audioPlayer.pause()
    }

    private fun removePendingAttachment(id: String) {
        _state.update {
            it.copy(pendingAttachments = it.pendingAttachments.filterNot { attachment -> attachment.id == id })
        }
    }

    private fun openImageViewer(images: List<MessageAttachment>, index: Int) {
        _state.update {
            it.copy(imageViewer = ImageViewerState(images = images, initialIndex = index))
        }
    }

    private fun closeImageViewer() {
        _state.update { it.copy(imageViewer = null) }
    }

    private fun saveImage(image: MessageAttachment) {
        viewModelScope.launch {
            imageSaver
                .saveToGallery(image)
                .onSuccess {
                    eventChannel.send(ChatDetailEvent.OnInfo(UiText.Resource(Res.string.image_saved)))
                }
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun List<MessageUi>.withUploadProgress(uploadProgress: Map<String, Float>): List<MessageUi> {
        if (uploadProgress.isEmpty()) {
            return this
        }
        return map { message ->
            if (message is MessageUi.LocalUserMessage && message.id in uploadProgress) {
                message.copy(uploadProgress = uploadProgress[message.id])
            } else message
        }
    }

    private fun observeCanSendMessage() {
        canSendMessage.onEach { canSend ->
            _state.update {
                it.copy(
                    canSendMessage = canSend
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun observeSelectedChat() {
        _chatId
            .onEach { chatId ->
                if (chatId != null) {
                    setupPaginatorForChat(chatId)
                    loadNextItems()
                } else {
                    currentPaginator = null
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTypingUsers() {
        _chatId
            .flatMapLatest { chatId ->
                if (chatId != null) {
                    typingRepository.observeTypingUserIds(chatId)
                } else flowOf(emptyList())
            }
            .onEach { typingUserIds ->
                _state.update {
                    it.copy(
                        typingUserIds = typingUserIds
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLocalUserTyping() {
        snapshotFlow { _state.value.messageTextFieldState.text.toString() }
            .drop(1)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .onEach {
                _chatId.value?.let { chatId ->
                    typingRepository.onLocalUserTyping(chatId)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Marks the open chat as read whenever a new message from someone else shows up while the
     * chat is on screen, which turns the sender's ticks into read ticks.
     */
    private fun observeReadMessages() {
        val newestMessageFromOthers = combine(
            chatInfoFlow,
            sessionStorage.observeAuthInfo()
        ) { chatInfo, authInfo ->
            val localUserId = authInfo?.user?.id ?: return@combine null
            chatInfo.messages
                .filter { it.sender.userId != localUserId }
                .maxByOrNull { it.message.createdAt }
                ?.let { chatInfo.chat.id to it.message.id }
        }

        combine(newestMessageFromOthers, _isScreenVisible) { newest, isVisible ->
            newest.takeIf { isVisible }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { (chatId, _) ->
                chatRepository.markChatRead(chatId)
                pushNotificationService.clearChatNotifications(chatId)
            }
            .launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                if (connectionState == ConnectionState.CONNECTED) {
                    currentPaginator?.loadNextItems()
                }

                _state.update {
                    it.copy(
                        connectionState = connectionState
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setupPaginatorForChat(chatId: String) {
        currentPaginator = Paginator(
            initialKey = null,
            onLoadUpdated = { isLoading ->
                _state.update { it.copy(isPaginationLoading = isLoading) }
            },
            onRequest = { beforeTimestamp ->
                messageRepository.fetchMessages(chatId, beforeTimestamp)
            },
            getNextKey = { messages ->
                messages.minOfOrNull { it.createdAt }?.toString()
            },
            onError = { throwable ->
                if (throwable is DataErrorException) {
                    _state.update {
                        it.copy(
                            paginationError = throwable.error.toUiText()
                        )
                    }
                }
            },
            onSuccess = { messages, _ ->
                _state.update {
                    it.copy(
                        endReached = messages.isEmpty(),
                        paginationError = null
                    )
                }
            }
        )

        _state.update {
            it.copy(
                endReached = false,
                isPaginationLoading = false,
            )
        }
    }

    private fun onLeaveChatClick() {
        _state.update {
            it.copy(
                isChatOptionsOpen = false
            )
        }

        val chatUi = state.value.chatUi
        // Leaving as the admin deletes the chat for everyone, so ask first
        if (chatUi != null && chatUi.isLocalUserAdmin && !chatUi.isLocalUserRemoved) {
            _state.update {
                it.copy(
                    isLeaveChatConfirmationVisible = true
                )
            }
        } else {
            leaveChat()
        }
    }

    private fun dismissLeaveChatConfirmation() {
        _state.update {
            it.copy(
                isLeaveChatConfirmationVisible = false
            )
        }
    }

    private fun leaveChat() {
        val chatId = _chatId.value ?: return

        dismissLeaveChatConfirmation()

        viewModelScope.launch {
            chatRepository
                .leaveChat(chatId)
                .onSuccess {
                    closeChat()
                }
                .onFailure { error ->
                    eventChannel.send(
                        ChatDetailEvent.OnError(
                            error.toUiText()
                        )
                    )
                }
        }
    }

    private suspend fun closeChat() {
        _state.value.messageTextFieldState.clearText()

        _chatId.update { null }
        _state.update {
            it.copy(
                chatUi = null,
                messages = emptyList(),
                bannerState = BannerState()
            )
        }

        eventChannel.send(
            ChatDetailEvent.OnChatLeft
        )
    }

    private fun observeChatEvents() {
        connectionClient
            .chatEvents
            .onEach { event ->
                when (event) {
                    is ChatEvent.ParticipantRemoved -> onParticipantRemoved(event)
                    is ChatEvent.ChatDeleted -> {
                        if (event.chatId == _chatId.value) {
                            closeChat()
                        }
                    }
                    is ChatEvent.UserTyping -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun onParticipantRemoved(event: ChatEvent.ParticipantRemoved) {
        if (event.chatId != _chatId.value) {
            return
        }

        val username = event.username
        val message = when {
            event.isLocalUser -> UiText.Resource(Res.string.you_were_removed_from_chat)
            username != null -> UiText.Resource(
                Res.string.x_was_removed_from_chat,
                arrayOf(username)
            )
            else -> UiText.Resource(Res.string.member_was_removed_from_chat)
        }
        eventChannel.send(ChatDetailEvent.OnInfo(message))
    }

    private fun onDismissChatOptions() {
        _state.update {
            it.copy(
                isChatOptionsOpen = false
            )
        }
    }

    private fun onChatOptionsClick() {
        _state.update {
            it.copy(
                isChatOptionsOpen = true
            )
        }
    }

    private fun switchChat(chatId: String?) {
        if (chatId != _chatId.value) {
            cancelRecording()
            audioPlayer.pause()
        }
        _chatId.update { chatId }
        _state.update { it.copy(pendingAttachments = emptyList(), imageViewer = null) }
        chatId?.let(pushNotificationService::clearChatNotifications)
        viewModelScope.launch {
            chatId?.let {
                chatRepository.fetchChatById(chatId)
            }
        }
    }

    private companion object {
        const val MAX_ATTACHMENTS = 10
        const val MAX_WAVEFORM_BARS = 40
        const val AMPLITUDE_POLL_INTERVAL_MS = 100L
        const val MIN_RECORDING_DURATION_MS = 1_000L
        const val MAX_RECORDING_DURATION_MS = 10 * 60 * 1_000L
    }
}

private data class ChatContent(
    val chatUi: ChatUi,
    val messages: List<MessageUi>
)
