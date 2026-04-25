package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.network.LoginRequest
import com.xinyu.ai.core.network.RegisterRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.domain.repository.AuthRepository
import com.xinyu.ai.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAuthRepository @Inject constructor(
    private val apiService: XinyuApiService,
    private val preferencesRepository: PreferencesRepository,
) : AuthRepository {
    override suspend fun login(email: String, password: String): AppResult<Unit> {
        return when (val result = safeApiCall { apiService.login(LoginRequest(email, password)) }) {
            is AppResult.Success -> {
                preferencesRepository.saveAuthSession(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                )
                AppResult.Success(Unit)
            }

            is AppResult.Error -> result
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        nickname: String,
    ): AppResult<Unit> {
        return when (
            val result = safeApiCall {
                apiService.register(
                    RegisterRequest(
                        email = email,
                        password = password,
                        nickname = nickname,
                    ),
                )
            }
        ) {
            is AppResult.Success -> {
                preferencesRepository.saveAuthSession(
                    accessToken = result.data.accessToken,
                    refreshToken = result.data.refreshToken,
                )
                AppResult.Success(Unit)
            }

            is AppResult.Error -> result
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        preferencesRepository.clearAuthSession()
        return AppResult.Success(Unit)
    }
}
