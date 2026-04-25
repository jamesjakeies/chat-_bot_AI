package com.xinyu.ai.domain.repository

import com.xinyu.ai.core.common.AppResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<Unit>

    suspend fun register(
        email: String,
        password: String,
        nickname: String,
    ): AppResult<Unit>

    suspend fun logout(): AppResult<Unit>
}
