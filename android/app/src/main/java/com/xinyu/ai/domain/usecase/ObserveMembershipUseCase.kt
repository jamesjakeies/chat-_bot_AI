package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.MembershipStatus
import com.xinyu.ai.domain.repository.SubscriptionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMembershipUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
) {
    operator fun invoke(): Flow<MembershipStatus?> = repository.observeMembership()
}
