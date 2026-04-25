package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.RoleRepository
import javax.inject.Inject

class RefreshRoleUseCase @Inject constructor(
    private val repository: RoleRepository,
) {
    suspend operator fun invoke(roleId: String) {
        repository.refreshRole(roleId)
    }
}
