package com.xinyu.ai.domain.model

data class ChatSession(
    val id: String,
    val userId: String,
    val roleId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val riskLevel: ChatRiskLevel = ChatRiskLevel.NORMAL,
)
