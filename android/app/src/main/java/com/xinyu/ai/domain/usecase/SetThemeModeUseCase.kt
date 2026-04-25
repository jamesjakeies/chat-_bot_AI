package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.AppThemeMode
import com.xinyu.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }
}
