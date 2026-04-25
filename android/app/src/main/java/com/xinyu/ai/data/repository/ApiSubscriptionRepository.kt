package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.network.MockUpgradeRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.core.network.toDomain
import com.xinyu.ai.domain.model.MembershipStatus
import com.xinyu.ai.domain.model.MembershipTier
import com.xinyu.ai.domain.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class ApiSubscriptionRepository @Inject constructor(
    private val apiService: XinyuApiService,
) : SubscriptionRepository {
    private val membership = MutableStateFlow<MembershipStatus?>(null)

    override fun observeMembership(): Flow<MembershipStatus?> = membership

    override suspend fun refreshMembership() {
        when (val result = safeApiCall { apiService.getSubscriptionMe() }) {
            is AppResult.Success -> membership.value = result.data.toDomain()
            is AppResult.Error -> Unit
        }
    }

    override suspend fun mockUpgrade(tier: MembershipTier): AppResult<Unit> {
        return when (
            val result = safeApiCall {
                apiService.mockUpgrade(MockUpgradeRequest(plan = tier.toPlanName()))
            }
        ) {
            is AppResult.Success -> {
                membership.value = result.data.toDomain()
                AppResult.Success(Unit)
            }

            is AppResult.Error -> result
        }
    }

    private fun MembershipTier.toPlanName(): String {
        return when (this) {
            MembershipTier.FREE -> "FREE"
            MembershipTier.PLUS -> "MONTHLY"
            MembershipTier.PREMIUM -> "PREMIUM"
        }
    }
}
