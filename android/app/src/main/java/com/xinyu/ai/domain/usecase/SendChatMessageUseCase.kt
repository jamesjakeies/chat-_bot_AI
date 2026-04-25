package com.xinyu.ai.domain.usecase

import com.xinyu.ai.domain.model.ChatSceneMode
import com.xinyu.ai.domain.repository.ChatRepository
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository,
) {
    suspend operator fun invoke(
        roleId: String,
        content: String,
        sceneMode: ChatSceneMode = ChatSceneMode.DEFAULT,
    ) {
        repository.sendUserMessage(roleId = roleId, content = content, sceneMode = sceneMode)
    }
}
