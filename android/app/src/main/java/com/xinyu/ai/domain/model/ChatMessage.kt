package com.xinyu.ai.domain.model

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val roleId: String,
    val senderType: MessageSenderType,
    val content: String,
    val createdAt: Long,
    val safetyLabel: String? = "NORMAL",
    val isRememberCandidate: Boolean = false,
)
