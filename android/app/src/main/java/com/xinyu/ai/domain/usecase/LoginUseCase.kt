package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        return repository.login(email, password)
    }
}
