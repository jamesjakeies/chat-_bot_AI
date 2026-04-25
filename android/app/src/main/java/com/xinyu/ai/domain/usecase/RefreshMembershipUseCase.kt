package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.SubscriptionRepository
import javax.inject.Inject

class RefreshMembershipUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
) {
    suspend operator fun invoke() {
        repository.refreshMembership()
    }
}
