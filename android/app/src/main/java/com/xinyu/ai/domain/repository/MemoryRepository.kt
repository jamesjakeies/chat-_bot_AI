package com.xinyu.ai.domain.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun observeMemories(): Flow<List<MemoryItem>>

    suspend fun refreshMemories(includePending: Boolean = true)

    suspend fun createMemory(
        roleId: String,
        content: String,
        sourceMessageId: String? = null,
        sensitive: Boolean = false,
    ): AppResult<MemoryItem>

    suspend fun confirmSensitive(memoryId: String, accepted: Boolean): AppResult<Unit>

    suspend fun deleteMemory(memoryId: String): AppResult<Unit>

    suspend fun updateRoleMemorySetting(roleId: String, enabled: Boolean): AppResult<Unit>
}
