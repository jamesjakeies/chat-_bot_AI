package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.UserProfile
import com.xinyu.ai.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeCurrentUser()
}
