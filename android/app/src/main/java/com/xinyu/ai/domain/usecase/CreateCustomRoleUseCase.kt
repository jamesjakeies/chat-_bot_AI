package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.CustomRoleDraft
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.repository.RoleRepository
import javax.inject.Inject

class CreateCustomRoleUseCase @Inject constructor(
    private val repository: RoleRepository,
) {
    suspend operator fun invoke(draft: CustomRoleDraft): AppResult<RoleProfile> {
        return repository.createCustomRole(draft)
    }
}
