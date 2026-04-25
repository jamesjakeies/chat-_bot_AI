package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.repository.MoodRepository
import javax.inject.Inject

class SubmitMoodUseCase @Inject constructor(
    private val repository: MoodRepository,
) {
    suspend operator fun invoke(
        moodLabel: String,
        pressureSources: List<String>,
        note: String?,
    ): AppResult<Unit> {
        return repository.submitMood(moodLabel, pressureSources, note)
    }
}
