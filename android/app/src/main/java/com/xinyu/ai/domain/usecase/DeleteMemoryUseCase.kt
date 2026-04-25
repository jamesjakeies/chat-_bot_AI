package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject

class DeleteMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    suspend operator fun invoke(memoryId: String): AppResult<Unit> {
        return repository.deleteMemory(memoryId)
    }
}
