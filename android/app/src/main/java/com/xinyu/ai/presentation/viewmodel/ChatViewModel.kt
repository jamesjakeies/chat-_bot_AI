package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.model.ChatSceneMode
import com.xinyu.ai.domain.model.ChatSession
import com.xinyu.ai.domain.model.QuickSceneAction
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.usecase.CreateMemoryUseCase
import com.xinyu.ai.domain.usecase.DeleteChatMessageUseCase
import com.xinyu.ai.domain.usecase.ObserveChatMessagesUseCase
import com.xinyu.ai.domain.usecase.ObserveChatSessionUseCase
import com.xinyu.ai.domain.usecase.ObserveRoleUseCase
import com.xinyu.ai.domain.usecase.RefreshChatMessagesUseCase
import com.xinyu.ai.domain.usecase.RefreshRoleUseCase
import com.xinyu.ai.domain.usecase.ReportChatMessageUseCase
import com.xinyu.ai.domain.usecase.SendChatMessageUseCase
import com.xinyu.ai.domain.usecase.ToggleRememberCandidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val role: RoleProfile? = null,
    val session: ChatSession? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isAiTyping: Boolean = false,
    val memoryState: UiState<Unit> = UiState.Idle,
    val quickActions: List<QuickSceneAction> = listOf(
        QuickSceneAction("安慰我", ChatSceneMode.COMFORT_MODE, "安慰我"),
        QuickSceneAction("听我吐槽", ChatSceneMode.VENT_MODE, "听我吐槽"),
        QuickSceneAction("哄我睡觉", ChatSceneMode.SLEEP_MODE, "哄我睡觉"),
        QuickSceneAction("帮我复盘", ChatSceneMode.REVIEW_MODE, "帮我复盘"),
        QuickSceneAction("给我鼓励", ChatSceneMode.ENCOURAGE_MODE, "给我鼓励"),
        QuickSceneAction("陪我冷静", ChatSceneMode.CALM_MODE, "陪我冷静"),
    ),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeRoleUseCase: ObserveRoleUseCase,
    observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    observeChatSessionUseCase: ObserveChatSessionUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val deleteChatMessageUseCase: DeleteChatMessageUseCase,
    private val toggleRememberCandidateUseCase: ToggleRememberCandidateUseCase,
    private val reportChatMessageUseCase: ReportChatMessageUseCase,
    private val refreshRoleUseCase: RefreshRoleUseCase,
    private val refreshChatMessagesUseCase: RefreshChatMessagesUseCase,
    private val createMemoryUseCase: CreateMemoryUseCase,
) : ViewModel() {
    private val roleId: String = checkNotNull(savedStateHandle["roleId"])
    private val isAiTyping = MutableStateFlow(false)
    private val memoryState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    val uiState: StateFlow<ChatUiState> = combine(
        observeRoleUseCase(roleId),
        observeChatSessionUseCase(roleId),
        observeChatMessagesUseCase(roleId),
        isAiTyping,
        memoryState,
    ) { role, session, messages, typing, memory ->
        ChatUiState(
            role = role,
            session = session,
            messages = messages,
            isAiTyping = typing,
            memoryState = memory,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatUiState(),
    )

    init {
        viewModelScope.launch {
            refreshRoleUseCase(roleId)
            refreshChatMessagesUseCase(roleId)
        }
    }

    fun sendMessage(
        content: String,
        sceneMode: ChatSceneMode = ChatSceneMode.DEFAULT,
    ) {
        if (content.isBlank() || isAiTyping.value) return

        viewModelScope.launch {
            isAiTyping.value = true
            try {
                sendChatMessageUseCase(
                    roleId = roleId,
                    content = content,
                    sceneMode = sceneMode,
                )
            } finally {
                isAiTyping.value = false
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            deleteChatMessageUseCase(roleId, messageId)
        }
    }

    fun toggleRememberCandidate(messageId: String) {
        viewModelScope.launch {
            toggleRememberCandidateUseCase(roleId, messageId)
        }
    }

    fun rememberMessage(message: ChatMessage, sensitive: Boolean) {
        viewModelScope.launch {
            memoryState.value = UiState.Loading
            when (
                val result = createMemoryUseCase(
                    roleId = roleId,
                    content = message.content,
                    sourceMessageId = message.id,
                    sensitive = sensitive,
                )
            ) {
                is AppResult.Success -> {
                    if (!message.isRememberCandidate) {
                        toggleRememberCandidateUseCase(roleId, message.id)
                    }
                    memoryState.value = UiState.Success(Unit)
                }

                is AppResult.Error -> {
                    memoryState.value = UiState.Error(result.error.asMessage())
                }
            }
        }
    }

    fun clearMemoryState() {
        memoryState.value = UiState.Idle
    }

    fun reportMessage(messageId: String) {
        viewModelScope.launch {
            reportChatMessageUseCase(roleId, messageId)
        }
    }
}
