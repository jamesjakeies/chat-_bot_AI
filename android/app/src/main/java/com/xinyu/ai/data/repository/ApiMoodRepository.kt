package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.network.CreateMoodRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.core.network.toDomain
import com.xinyu.ai.domain.model.MoodLog
import com.xinyu.ai.domain.model.MoodWeeklySummary
import com.xinyu.ai.domain.repository.MoodRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class ApiMoodRepository @Inject constructor(
    private val apiService: XinyuApiService,
) : MoodRepository {
    private val recentMoods = MutableStateFlow<List<MoodLog>>(emptyList())
    private val weeklySummary = MutableStateFlow<MoodWeeklySummary?>(null)

    override fun observeRecentMoods(): Flow<List<MoodLog>> = recentMoods

    override fun observeWeeklySummary(): Flow<MoodWeeklySummary?> = weeklySummary

    override suspend fun refreshRecentMoods() {
        when (val result = safeApiCall { apiService.getRecentMoods() }) {
            is AppResult.Success -> recentMoods.value = result.data.map { it.toDomain() }
            is AppResult.Error -> Unit
        }
    }

    override suspend fun refreshWeeklySummary() {
        when (val result = safeApiCall { apiService.getWeeklyMoodSummary() }) {
            is AppResult.Success -> weeklySummary.value = result.data.toDomain()
            is AppResult.Error -> weeklySummary.value = null
        }
    }

    override suspend fun submitMood(
        moodLabel: String,
        pressureSources: List<String>,
        note: String?,
    ): AppResult<Unit> {
        return when (
            val result = safeApiCall {
                apiService.createMood(
                    CreateMoodRequest(
                        moodLabel = moodLabel,
                        pressureSources = pressureSources,
                        note = note?.takeIf { it.isNotBlank() },
                    ),
                )
            }
        ) {
            is AppResult.Success -> {
                refreshRecentMoods()
                refreshWeeklySummary()
                AppResult.Success(Unit)
            }

            is AppResult.Error -> result
        }
    }
}
