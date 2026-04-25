package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.MoodLog
import com.xinyu.ai.domain.repository.MoodRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRecentMoodsUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    operator fun invoke(): Flow<List<MoodLog>> = repository.observeRecentMoods()
}
