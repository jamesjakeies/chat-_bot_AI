package com.xinyu.ai.domain.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.MembershipStatus
import com.xinyu.ai.domain.model.MembershipTier
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeMembership(): Flow<MembershipStatus?>

    suspend fun refreshMembership()

    suspend fun mockUpgrade(tier: MembershipTier): AppResult<Unit>
}
