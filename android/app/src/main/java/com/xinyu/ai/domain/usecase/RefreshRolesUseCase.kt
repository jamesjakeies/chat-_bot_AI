package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.RoleRepository
import javax.inject.Inject

class RefreshRolesUseCase @Inject constructor(
    private val repository: RoleRepository,
) {
    suspend operator fun invoke() {
        repository.refreshRoles()
    }
}
