package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject

class ConfirmSensitiveMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    suspend operator fun invoke(memoryId: String, accepted: Boolean): AppResult<Unit> {
        return repository.confirmSensitive(memoryId, accepted)
    }
}
