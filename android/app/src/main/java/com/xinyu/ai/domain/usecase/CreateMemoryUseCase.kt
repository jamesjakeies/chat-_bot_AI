package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject

class CreateMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    suspend operator fun invoke(
        roleId: String,
        content: String,
        sourceMessageId: String? = null,
        sensitive: Boolean = false,
    ): AppResult<MemoryItem> {
        return repository.createMemory(roleId, content, sourceMessageId, sensitive)
    }
}
