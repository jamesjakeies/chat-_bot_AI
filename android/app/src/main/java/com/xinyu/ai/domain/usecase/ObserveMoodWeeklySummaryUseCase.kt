package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.MoodWeeklySummary
import com.xinyu.ai.domain.repository.MoodRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMoodWeeklySummaryUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    operator fun invoke(): Flow<MoodWeeklySummary?> = repository.observeWeeklySummary()
}
