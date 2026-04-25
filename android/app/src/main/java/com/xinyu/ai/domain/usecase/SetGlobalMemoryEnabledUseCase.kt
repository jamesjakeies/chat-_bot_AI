package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetGlobalMemoryEnabledUseCase @Inject constructor(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.setGlobalMemoryEnabled(enabled)
    }
}
