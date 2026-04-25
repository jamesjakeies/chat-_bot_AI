package com.xinyu.ai.domain.model

data class MoodLog(
    val id: String,
    val moodScore: Int,
    val moodLabel: String,
    val pressureSources: List<String>,
    val note: String?,
    val createdAt: Long,
)
