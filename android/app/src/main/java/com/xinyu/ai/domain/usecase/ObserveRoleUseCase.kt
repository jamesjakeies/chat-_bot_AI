package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRoleUseCase @Inject constructor(
    private val repository: RoleRepository,
) {
    operator fun invoke(roleId: String): Flow<RoleProfile?> = repository.observeRole(roleId)
}
