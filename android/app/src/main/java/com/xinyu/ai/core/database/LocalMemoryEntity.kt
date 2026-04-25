package com.xinyu.ai.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories_cache")
data class LocalMemoryEntity(
    @PrimaryKey val id: String,
    val roleId: String,
    val roleName: String,
    val content: String,
    val memoryType: String,
    val sensitivityLevel: String,
    val userConsented: Boolean,
    val createdAt: Long,
)
