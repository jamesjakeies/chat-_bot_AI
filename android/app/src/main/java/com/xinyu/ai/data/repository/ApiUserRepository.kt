package com.xinyu.ai.data.repository

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.NetworkError
import com.xinyu.ai.core.common.asMessage
import com.xinyu.ai.core.network.AgeVerificationRequest
import com.xinyu.ai.core.network.XinyuApiService
import com.xinyu.ai.core.network.safeApiCall
import com.xinyu.ai.core.network.toDomain
import com.xinyu.ai.domain.model.UserProfile
import com.xinyu.ai.domain.repository.PreferencesRepository
import com.xinyu.ai.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class ApiUserRepository @Inject constructor(
    private val apiService: XinyuApiService,
    private val preferencesRepository: PreferencesRepository,
) : UserRepository {
    private val currentUser = MutableStateFlow<UserProfile?>(null)

    override fun observeCurrentUser(): Flow<UserProfile?> = currentUser

    override suspend fun refreshCurrentUser() {
        when (val result = safeApiCall { apiService.getProfile() }) {
            is AppResult.Success -> {
                currentUser.value = result.data.toDomain()
            }

            is AppResult.Error -> {
                if (result.error is NetworkError.Unauthorized) {
                    preferencesRepository.clearAuthSession()
                    currentUser.value = null
                }
            }
        }
    }

    override suspend fun verifyAge(
        birthYear: Int,
        ageVerified: Boolean,
        guardianConsent: Boolean,
    ) {
        when (
            val result = safeApiCall {
                apiService.verifyAge(
                    AgeVerificationRequest(
                        birthYear = birthYear,
                        ageVerified = ageVerified,
                        guardianConsent = guardianConsent,
                    ),
                )
            }
        ) {
            is AppResult.Success -> {
                currentUser.value = result.data.toDomain()
            }

            is AppResult.Error -> {
                if (result.error is NetworkError.Unauthorized) {
                    preferencesRepository.clearAuthSession()
                    currentUser.value = null
                }
                throw IllegalStateException(result.error.asMessage())
            }
        }
    }
}
