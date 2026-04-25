package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.network.CreateCustomRoleRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.toDomain
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.domain.model.CustomRoleDraft
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleCategory
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.model.SafetyLevel
import com.xinyu.ai.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRoleRepository @Inject constructor(
    private val apiService: XinyuApiService,
) : RoleRepository {
    private val roles = MutableStateFlow<List<RoleProfile>>(emptyList())
    private val favoriteRoleIds = MutableStateFlow<Set<String>>(emptySet())

    override fun observeRoles(): Flow<List<RoleProfile>> = roles

    override fun observeRole(roleId: String): Flow<RoleProfile?> {
        return roles.map { currentRoles -> currentRoles.firstOrNull { it.id == roleId } }
    }

    override suspend fun refreshRoles() {
        when (val result = safeApiCall { apiService.getRoles() }) {
            is AppResult.Success -> roles.value = result.data.map { it.toDomain() }
            is AppResult.Error -> Unit
        }
    }

    override suspend fun refreshRole(roleId: String) {
        when (val result = safeApiCall { apiService.getRole(roleId) }) {
            is AppResult.Success -> {
                val role = result.data.toDomain()
                roles.update { current ->
                    val remaining = current.filterNot { it.id == roleId }
                    (remaining + role).sortedBy { it.name }
                }
            }

            is AppResult.Error -> Unit
        }
    }

    override fun observeFavoriteRoleIds(): Flow<Set<String>> = favoriteRoleIds

    override suspend fun toggleFavoriteRole(roleId: String) {
        favoriteRoleIds.update { current ->
            if (roleId in current) current - roleId else current + roleId
        }
    }

    override suspend fun createCustomRole(draft: CustomRoleDraft): AppResult<RoleProfile> {
        val result = safeApiCall {
            apiService.createCustomRole(
                CreateCustomRoleRequest(
                    name = draft.name,
                    avatarUrl = draft.avatarUrl,
                    category = draft.category.toApiCategory(),
                    relationshipType = draft.relationshipType.toApiRelationshipType(),
                    personality = draft.personality,
                    speechStyle = draft.speechStyle,
                    systemPrompt = draft.systemPrompt,
                    safetyLevel = draft.safetyLevel.toApiSafetyLevel(),
                    isAdultOnly = draft.isAdultOnly || draft.relationshipType == RelationshipType.ROMANTIC_PARTNER,
                ),
            )
        }

        return when (result) {
            is AppResult.Success -> {
                val role = result.data.toDomain()
                roles.update { current ->
                    (current.filterNot { it.id == role.id } + role)
                        .sortedWith(compareByDescending<RoleProfile> { it.isOfficial }.thenBy { it.name })
                }
                AppResult.Success(role)
            }

            is AppResult.Error -> result
        }
    }

    private fun RoleCategory.toApiCategory(): String {
        return when (this) {
            RoleCategory.EMOTIONAL_SUPPORT -> "EMOTIONAL_SUPPORT"
            RoleCategory.ROMANCE -> "ROMANTIC_COMPANION"
            RoleCategory.SLEEP -> "SLEEP_COMPANION"
            RoleCategory.STUDY -> "STUDY_BUDDY"
            RoleCategory.CAREER -> "CAREER_MENTOR"
            RoleCategory.CUSTOM -> "CUSTOM_ROLE"
        }
    }

    private fun RelationshipType.toApiRelationshipType(): String {
        return when (this) {
            RelationshipType.LISTENER -> "LISTENER"
            RelationshipType.SUPPORT_PARTNER -> "SUPPORT_PARTNER"
            RelationshipType.ROMANTIC_PARTNER -> "ROMANTIC_PARTNER"
            RelationshipType.STUDY_BUDDY -> "STUDY_BUDDY"
            RelationshipType.BEDTIME_COMPANION -> "BEDTIME_COMPANION"
            RelationshipType.MENTOR -> "CAREER_MENTOR"
            RelationshipType.CUSTOM -> "CUSTOM"
        }
    }

    private fun SafetyLevel.toApiSafetyLevel(): String {
        return name
    }
}
