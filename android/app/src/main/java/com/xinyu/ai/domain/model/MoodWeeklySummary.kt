package com.xinyu.ai.domain.model

data class MoodWeeklySummary(
    val checkInCount: Int,
    val mainMood: String,
    val frequentPressureSource: String,
    val gentleSuggestion: String,
    val recommendedRoles: List<String>,
    val disclaimer: String,
)
