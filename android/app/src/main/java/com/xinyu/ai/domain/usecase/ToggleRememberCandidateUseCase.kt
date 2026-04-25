package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.ChatRepository
import javax.inject.Inject

class ToggleRememberCandidateUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(roleId: String, messageId: String) {
        repository.toggleRememberCandidate(roleId, messageId)
    }
}
