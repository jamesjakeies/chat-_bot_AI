package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.UiState
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.domain.model.MoodLog
import com.xinyu.ai.domain.model.MoodWeeklySummary
import com.xinyu.ai.domain.usecase.ObserveMoodWeeklySummaryUseCase
import com.xinyu.ai.domain.usecase.ObserveRecentMoodsUseCase
import com.xinyu.ai.domain.usecase.RefreshMoodDataUseCase
import com.xinyu.ai.domain.usecase.SubmitMoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoodCheckInUiState(
    val recentMoods: List<MoodLog> = emptyList(),
    val weeklySummary: MoodWeeklySummary? = null,
    val submitState: UiState<Unit> = UiState.Idle,
)

@HiltViewModel
class MoodCheckInViewModel @Inject constructor(
    observeRecentMoodsUseCase: ObserveRecentMoodsUseCase,
    observeMoodWeeklySummaryUseCase: ObserveMoodWeeklySummaryUseCase,
    private val refreshMoodDataUseCase: RefreshMoodDataUseCase,
    private val submitMoodUseCase: SubmitMoodUseCase,
) : ViewModel() {
    private val submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)

    val uiState: StateFlow<MoodCheckInUiState> = combine(
        observeRecentMoodsUseCase(),
        observeMoodWeeklySummaryUseCase(),
        submitState,
    ) { moods, summary, state ->
        MoodCheckInUiState(
            recentMoods = moods,
            weeklySummary = summary,
            submitState = state,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MoodCheckInUiState(),
    )

    init {
        viewModelScope.launch {
            refreshMoodDataUseCase()
        }
    }

    fun submit(moodLabel: String, pressureSources: List<String>, note: String) {
        viewModelScope.launch {
            submitState.value = UiState.Loading
            when (
                val result = submitMoodUseCase(
                    moodLabel = moodLabel,
                    pressureSources = pressureSources,
                    note = note.takeIf { it.isNotBlank() },
                )
            ) {
                is AppResult.Success -> submitState.value = UiState.Success(Unit)
                is AppResult.Error -> submitState.value = UiState.Error(result.error.asMessage())
            }
        }
    }

    fun clearSubmitState() {
        submitState.value = UiState.Idle
    }
}
