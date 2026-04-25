package com.xinyu.ai.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages_cache")
data class LocalChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val roleId: String,
    val senderType: String,
    val content: String,
    val createdAt: Long,
    val safetyLabel: String?,
    val isRememberCandidate: Boolean,
)
