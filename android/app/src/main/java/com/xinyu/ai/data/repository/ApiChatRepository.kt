package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.NetworkError
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.core.database.LocalChatMessageDao
import com.xinyu.ai.core.database.LocalChatMessageEntity
import com.xinyu.ai.core.network.ChatMessageDto
import com.xinyu.ai.core.network.CreateChatSessionRequest
import com.xinyu.ai.core.network.CreateReportRequest
import com.xinyu.ai.core.network.SendMessageRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.core.network.toDomain
import com.xinyu.ai.core.network.toEntity
import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.model.ChatRiskLevel
import com.xinyu.ai.domain.model.ChatSceneMode
import com.xinyu.ai.domain.model.ChatSession
import com.xinyu.ai.domain.repository.ChatRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ApiChatRepository @Inject constructor(
    private val apiService: XinyuApiService,
    private val localChatMessageDao: LocalChatMessageDao,
) : ChatRepository {
    private val sessionsByRole = MutableStateFlow<Map<String, ChatSession>>(emptyMap())

    override fun observeSession(roleId: String): Flow<ChatSession?> {
        return sessionsByRole.map { it[roleId] }
    }

    override fun observeMessages(roleId: String): Flow<List<ChatMessage>> {
        return localChatMessageDao.observeMessages(roleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshSession(roleId: String) {
        val existing = sessionsByRole.value[roleId]
        if (existing != null) return

        when (val result = safeApiCall { apiService.getChatSessions() }) {
            is AppResult.Success -> {
                val matched = result.data
                    .map { it.toDomain() }
                    .filter { it.roleId == roleId }
                    .maxByOrNull { it.updatedAt }

                val session = matched ?: createSession(roleId)
                sessionsByRole.value = sessionsByRole.value + (roleId to session)
            }

            is AppResult.Error -> Unit
        }
    }

    override suspend fun refreshMessages(roleId: String) {
        val session = ensureSession(roleId) ?: return

        when (val result = safeApiCall { apiService.getChatMessages(session.id) }) {
            is AppResult.Success -> replaceMessagesFromRemote(roleId, result.data)
            is AppResult.Error -> Unit
        }
    }

    override suspend fun sendUserMessage(
        roleId: String,
        content: String,
        sceneMode: ChatSceneMode,
    ) {
        val session = ensureSession(roleId)
        if (session == null) {
            upsertLocalSystemMessage(
                roleId = roleId,
                sessionId = "local-$roleId",
                content = "暂时无法连接聊天服务，请稍后再试。",
            )
            return
        }

        val optimisticId = "local-${UUID.randomUUID()}"
        localChatMessageDao.upsert(
            LocalChatMessageEntity(
                id = optimisticId,
                sessionId = session.id,
                roleId = roleId,
                senderType = "USER",
                content = content,
                createdAt = System.currentTimeMillis(),
                safetyLabel = "NORMAL",
                isRememberCandidate = false,
            ),
        )

        val payload = SendMessageRequest(
            content = content,
            sceneMode = sceneMode.toApiName(),
        )

        when (val result = safeApiCall { apiService.sendMessage(session.id, payload) }) {
            is AppResult.Success -> {
                localChatMessageDao.deleteMessage(optimisticId)
                sessionsByRole.value = sessionsByRole.value + (roleId to result.data.session.toDomain())
                upsertRemoteMessages(roleId, result.data.messages)
            }

            is AppResult.Error -> {
                localChatMessageDao.deleteMessage(optimisticId)
                upsertLocalSystemMessage(
                    roleId = roleId,
                    sessionId = session.id,
                    content = result.error.chatErrorMessage(),
                    riskLevel = ChatRiskLevel.ATTENTION,
                )
            }
        }
    }

    override suspend fun deleteMessage(roleId: String, messageId: String) {
        localChatMessageDao.deleteMessage(messageId)
    }

    override suspend fun toggleRememberCandidate(roleId: String, messageId: String) {
        val existing = localChatMessageDao.observeMessages(roleId)
            .first()
            .firstOrNull { it.id == messageId } ?: return

        localChatMessageDao.updateRememberCandidate(
            messageId = messageId,
            remember = !existing.isRememberCandidate,
        )
    }

    override suspend fun reportMessage(roleId: String, messageId: String) {
        safeApiCall {
            apiService.createReport(
                CreateReportRequest(
                    messageId = messageId,
                    reason = "用户从聊天页长按举报",
                ),
            )
        }
    }

    private suspend fun ensureSession(roleId: String): ChatSession? {
        runCatching {
            refreshSession(roleId)
        }

        return sessionsByRole.value[roleId]
    }

    private suspend fun createSession(roleId: String): ChatSession {
        return when (
            val result = safeApiCall {
                apiService.createChatSession(CreateChatSessionRequest(roleId = roleId))
            }
        ) {
            is AppResult.Success -> result.data.toDomain()
            is AppResult.Error -> throw IllegalStateException("无法创建聊天会话")
        }
    }

    private suspend fun replaceMessagesFromRemote(
        roleId: String,
        messages: List<ChatMessageDto>,
    ) {
        val existingMessages = localChatMessageDao.observeMessages(roleId).first()
        val rememberById = existingMessages.associate { it.id to it.isRememberCandidate }
        localChatMessageDao.clearRole(roleId)
        localChatMessageDao.upsertAll(
            messages.map { dto ->
                dto.toDomain(
                    roleId = roleId,
                    rememberCandidate = rememberById[dto.id] ?: false,
                ).toEntity()
            },
        )
    }

    private suspend fun upsertRemoteMessages(
        roleId: String,
        messages: List<ChatMessageDto>,
    ) {
        val existingMessages = localChatMessageDao.observeMessages(roleId).first()
        val rememberById = existingMessages.associate { it.id to it.isRememberCandidate }
        localChatMessageDao.upsertAll(
            messages.map { dto ->
                dto.toDomain(
                    roleId = roleId,
                    rememberCandidate = rememberById[dto.id] ?: false,
                ).toEntity()
            },
        )
    }

    private suspend fun upsertLocalSystemMessage(
        roleId: String,
        sessionId: String,
        content: String,
        riskLevel: ChatRiskLevel = ChatRiskLevel.NORMAL,
    ) {
        localChatMessageDao.upsert(
            LocalChatMessageEntity(
                id = "local-system-${UUID.randomUUID()}",
                sessionId = sessionId,
                roleId = roleId,
                senderType = "SYSTEM",
                content = content,
                createdAt = System.currentTimeMillis(),
                safetyLabel = riskLevel.name,
                isRememberCandidate = false,
            ),
        )
    }

    private fun ChatSceneMode.toApiName(): String? {
        return when (this) {
            ChatSceneMode.DEFAULT -> null
            ChatSceneMode.COMFORT_MODE -> "comfort_mode"
            ChatSceneMode.VENT_MODE -> "vent_mode"
            ChatSceneMode.SLEEP_MODE -> "sleep_mode"
            ChatSceneMode.REVIEW_MODE -> "review_mode"
            ChatSceneMode.ENCOURAGE_MODE -> "encourage_mode"
            ChatSceneMode.CALM_MODE -> "calm_mode"
        }
    }

    private fun NetworkError.chatErrorMessage(): String {
        val message = asMessage()
        return if (
            message.contains("额度") ||
            message.contains("会员") ||
            message.contains("上限") ||
            message.contains("解锁")
        ) {
            message
        } else {
            "这条消息没有成功发出，请检查后端服务或稍后重试。"
        }
    }
}
