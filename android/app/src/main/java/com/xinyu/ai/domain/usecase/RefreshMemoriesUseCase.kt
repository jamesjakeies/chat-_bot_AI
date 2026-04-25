package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject

class RefreshMemoriesUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    suspend operator fun invoke(includePending: Boolean = true) {
        repository.refreshMemories(includePending)
    }
}
