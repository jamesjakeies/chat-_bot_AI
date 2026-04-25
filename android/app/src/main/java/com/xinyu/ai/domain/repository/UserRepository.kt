package com.xinyu.ai.domain.repository

import com.xinyu.ai.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeCurrentUser(): Flow<UserProfile?>

    suspend fun refreshCurrentUser()

    suspend fun verifyAge(
        birthYear: Int,
        ageVerified: Boolean,
        guardianConsent: Boolean,
    )
}
