package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.ChatSession
import com.xinyu.ai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatSessionUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    operator fun invoke(roleId: String): Flow<ChatSession?> = repository.observeSession(roleId)
}
