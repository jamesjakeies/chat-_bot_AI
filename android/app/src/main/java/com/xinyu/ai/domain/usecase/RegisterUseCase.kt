package com.xinyu.ai.domain.usecase

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        nickname: String,
    ): AppResult<Unit> {
        return repository.register(email, password, nickname)
    }
}
