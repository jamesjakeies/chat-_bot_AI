package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.UserRepository
import javax.inject.Inject

class RefreshCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke() {
        repository.refreshCurrentUser()
    }
}
