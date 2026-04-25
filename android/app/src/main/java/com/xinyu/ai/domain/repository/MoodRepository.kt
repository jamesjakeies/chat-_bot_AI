package com.xinyu.ai.domain.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.MoodLog
import com.xinyu.ai.domain.model.MoodWeeklySummary
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    fun observeRecentMoods(): Flow<List<MoodLog>>

    fun observeWeeklySummary(): Flow<MoodWeeklySummary?>

    suspend fun refreshRecentMoods()

    suspend fun refreshWeeklySummary()

    suspend fun submitMood(
        moodLabel: String,
        pressureSources: List<String>,
        note: String?,
    ): AppResult<Unit>
}
