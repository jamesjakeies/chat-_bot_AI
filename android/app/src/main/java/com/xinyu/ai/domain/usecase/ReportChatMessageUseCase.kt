package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.repository.ChatRepository
import javax.inject.Inject

class ReportChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(roleId: String, messageId: String) {
        repository.reportMessage(roleId, messageId)
    }
}
