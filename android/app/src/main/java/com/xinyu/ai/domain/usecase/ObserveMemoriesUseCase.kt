package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.MemoryItem
import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMemoriesUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    operator fun invoke(): Flow<List<MemoryItem>> = repository.observeMemories()
}
