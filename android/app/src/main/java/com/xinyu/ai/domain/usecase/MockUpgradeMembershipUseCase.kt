package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.model.MembershipTier
import com.xinyu.ai.domain.repository.SubscriptionRepository
import javax.inject.Inject

class MockUpgradeMembershipUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
) {
    suspend operator fun invoke(tier: MembershipTier): AppResult<Unit> {
        return repository.mockUpgrade(tier)
    }
}
