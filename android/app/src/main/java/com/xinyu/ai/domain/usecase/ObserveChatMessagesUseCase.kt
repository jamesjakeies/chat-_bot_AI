package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.ChatMessage
import com.xinyu.ai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveChatMessagesUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    operator fun invoke(roleId: String): Flow<List<ChatMessage>> = repository.observeMessages(roleId)
}
