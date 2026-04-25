package com.xinyu.ai.domain.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.CustomRoleDraft
import com.xinyu.ai.domain.model.RoleProfile
import kotlinx.coroutines.flow.Flow

interface RoleRepository {
    fun observeRoles(): Flow<List<RoleProfile>>

    fun observeRole(roleId: String): Flow<RoleProfile?>

    suspend fun refreshRoles()

    suspend fun refreshRole(roleId: String)

    fun observeFavoriteRoleIds(): Flow<Set<String>>

    suspend fun toggleFavoriteRole(roleId: String)

    suspend fun createCustomRole(draft: CustomRoleDraft): AppResult<RoleProfile>
}
