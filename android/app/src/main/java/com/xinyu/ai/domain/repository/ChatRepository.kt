package com.xinyu.ai.domain.repository

import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.model.ChatSceneMode
import com.xinyu.ai.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeSession(roleId: String): Flow<ChatSession?>

    fun observeMessages(roleId: String): Flow<List<ChatMessage>>

    suspend fun refreshSession(roleId: String)

    suspend fun refreshMessages(roleId: String)

    suspend fun sendUserMessage(
        roleId: String,
        content: String,
        sceneMode: ChatSceneMode = ChatSceneMode.DEFAULT,
    )

    suspend fun deleteMessage(roleId: String, messageId: String)

    suspend fun toggleRememberCandidate(roleId: String, messageId: String)

    suspend fun reportMessage(roleId: String, messageId: String)
}
