package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.CustomRoleDraft
import com.xinyu.ai.domain.model.RelationshipType
import com.xinyu.ai.domain.model.RoleCategory
import com.xinyu.ai.domain.model.RoleProfile
import com.xinyu.ai.domain.model.SafetyLevel
import com.xinyu.ai.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateCustomRoleUseCaseTest {
    @Test
    fun `invoke delegates draft to repository and returns created role`() = runTest {
        val repository = FakeRoleRepository()
        val useCase = CreateCustomRoleUseCase(repository)
        val draft = CustomRoleDraft(
            name = "清醒陪跑",
            category = RoleCategory.STUDY,
            relationshipType = RelationshipType.STUDY_BUDDY,
            personality = "清醒、耐心",
            speechStyle = "短句、具体",
            systemPrompt = "帮助用户学习，不制造依赖。",
            safetyLevel = SafetyLevel.STRICT,
        )

        val result = useCase(draft)

        assertTrue(result is AppResult.Success)
        assertEquals(draft, repository.lastDraft)
        assertEquals("清醒陪跑", (result as AppResult.Success).data.name)
    }
}

private class FakeRoleRepository : RoleRepository {
    var lastDraft: CustomRoleDraft? = null

    override fun observeRoles(): Flow<List<RoleProfile>> = flowOf(emptyList())

    override fun observeRole(roleId: String): Flow<RoleProfile?> = flowOf(null)

    override suspend fun refreshRoles() = Unit

    override suspend fun refreshRole(roleId: String) = Unit

    override fun observeFavoriteRoleIds(): Flow<Set<String>> = flowOf(emptySet())

    override suspend fun toggleFavoriteRole(roleId: String) = Unit

    override suspend fun createCustomRole(draft: CustomRoleDraft): AppResult<RoleProfile> {
        lastDraft = draft
        return AppResult.Success(
            RoleProfile(
                id = "custom_1",
                name = draft.name,
                avatarUrl = draft.avatarUrl,
                category = draft.category,
                relationshipType = draft.relationshipType,
                personalityTags = draft.personality.split("、"),
                shortDescription = draft.speechStyle,
                greeting = "你好，我是${draft.name}。",
                sampleReply = "我们先从一个小步骤开始。",
                isAdultOnly = draft.isAdultOnly,
                isOfficial = false,
                safetyLevel = draft.safetyLevel,
            ),
        )
    }
}
