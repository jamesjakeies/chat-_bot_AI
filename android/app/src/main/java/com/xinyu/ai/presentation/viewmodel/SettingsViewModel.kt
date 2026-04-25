package com.xinyu.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xinyu.ai.domain.model.AppSettings
import com.xinyu.ai.domain.model.AppThemeMode
import com.xinyu.ai.domain.usecase.ObserveSettingsUseCase
import com.xinyu.ai.domain.usecase.SetGlobalMemoryEnabledUseCase
import com.xinyu.ai.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setGlobalMemoryEnabledUseCase: SetGlobalMemoryEnabledUseCase,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = observeSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings(),
        )

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            setThemeModeUseCase(mode)
        }
    }

    fun updateGlobalMemory(enabled: Boolean) {
        viewModelScope.launch {
            setGlobalMemoryEnabledUseCase(enabled)
        }
    }
}
