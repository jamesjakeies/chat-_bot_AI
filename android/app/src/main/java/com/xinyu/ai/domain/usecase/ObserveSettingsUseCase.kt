package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.AppSettings
import com.xinyu.ai.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val repository: PreferencesRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}
