package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.MoodRepository
import javax.inject.Inject

class RefreshMoodDataUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke() {
        repository.refreshRecentMoods()
        repository.refreshWeeklySummary()
    }
}
