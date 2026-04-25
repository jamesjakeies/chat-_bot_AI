package com.xinyu.ai.domain.model

data class MemoryItem(
    val id: String,
    val roleId: String,
    val roleName: String,
    val content: String,
    val memoryType: String,
    val sensitivityLevel: String,
    val userConsented: Boolean,
    val isSensitive: Boolean,
    val createdAt: Long = 0L,
)
