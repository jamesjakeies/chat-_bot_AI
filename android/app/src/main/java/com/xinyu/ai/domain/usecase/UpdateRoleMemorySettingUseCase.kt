package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.repository.MemoryRepository
import javax.inject.Inject

class UpdateRoleMemorySettingUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    suspend operator fun invoke(roleId: String, enabled: Boolean): AppResult<Unit> {
        return repository.updateRoleMemorySetting(roleId, enabled)
    }
}
