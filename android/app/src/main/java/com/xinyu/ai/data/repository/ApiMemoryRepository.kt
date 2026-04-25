package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.database.LocalMemoryDao
import com.xinyu.ai.core.network.ConfirmSensitiveMemoryRequest
import com.xinyu.ai.core.network.CreateMemoryRequest
import com.xinyu.ai.core.network.RoleMemorySettingRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.core.network.toDomain
import com.xinyu.ai.core.network.toEntity
import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ApiMemoryRepository @Inject constructor(
    private val apiService: XinyuApiService,
    private val localMemoryDao: LocalMemoryDao,
) : MemoryRepository {
    override fun observeMemories(): Flow<List<MemoryItem>> {
        return localMemoryDao.observeMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshMemories(includePending: Boolean) {
        when (val result = safeApiCall { apiService.getMemories(includePending = includePending) }) {
            is AppResult.Success -> {
                localMemoryDao.clearAll()
                localMemoryDao.upsertAll(result.data.map { it.toEntity() })
            }

            is AppResult.Error -> Unit
        }
    }

    override suspend fun createMemory(
        roleId: String,
        content: String,
        sourceMessageId: String?,
        sensitive: Boolean,
    ): AppResult<MemoryItem> {
        val result = safeApiCall {
            apiService.createMemory(
                CreateMemoryRequest(
                    roleId = roleId,
                    content = content,
                    sensitivityLevel = if (sensitive) "SENSITIVE" else null,
                    userConsented = if (sensitive) false else true,
                    sourceMessageId = sourceMessageId,
                ),
            )
        }

        return when (result) {
            is AppResult.Success -> {
                val memory = result.data.toDomain()
                localMemoryDao.upsert(memory.toEntity())
                AppResult.Success(memory)
            }

            is AppResult.Error -> result
        }
    }

    override suspend fun confirmSensitive(
        memoryId: String,
        accepted: Boolean,
    ): AppResult<Unit> {
        val result = safeApiCall {
            apiService.confirmSensitiveMemory(
                ConfirmSensitiveMemoryRequest(
                    memoryId = memoryId,
                    accepted = accepted,
                ),
            )
        }

        return when (result) {
            is AppResult.Success -> {
                if (accepted) {
                    localMemoryDao.upsert(result.data.toEntity())
                } else {
                    localMemoryDao.delete(memoryId)
                }
                AppResult.Success(Unit)
            }

            is AppResult.Error -> result
        }
    }

    override suspend fun deleteMemory(memoryId: String): AppResult<Unit> {
        return when (val result = safeApiCall { apiService.deleteMemory(memoryId) }) {
            is AppResult.Success -> {
                localMemoryDao.delete(memoryId)
                AppResult.Success(Unit)
            }

            is AppResult.Error -> result
        }
    }

    override suspend fun updateRoleMemorySetting(
        roleId: String,
        enabled: Boolean,
    ): AppResult<Unit> {
        return when (
            val result = safeApiCall {
                apiService.updateRoleMemorySetting(
                    roleId,
                    RoleMemorySettingRequest(memoryEnabled = enabled),
                )
            }
        ) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> result
        }
    }
}
