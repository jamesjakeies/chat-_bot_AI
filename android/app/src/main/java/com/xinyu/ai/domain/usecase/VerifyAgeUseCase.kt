package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.UserRepository
import javax.inject.Inject

class VerifyAgeUseCase @Inject constructor(
    private val repository: UserRepository,
) {
    suspend operator fun invoke(
        birthYear: Int,
        ageVerified: Boolean,
        guardianConsent: Boolean,
    ) {
        repository.verifyAge(birthYear, ageVerified, guardianConsent)
    }
}
