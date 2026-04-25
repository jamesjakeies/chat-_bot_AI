package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteRoleIdsUseCase @Inject constructor(
    private val repository: RoleRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavoriteRoleIds()
}
