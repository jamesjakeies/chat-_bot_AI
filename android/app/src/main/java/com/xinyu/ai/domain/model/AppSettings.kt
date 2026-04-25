package com.xinyu.ai.domain.model

data class AppSettings(
    val authToken: String? = null,
    val refreshToken: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val globalMemoryEnabled: Boolean = true,
    val hasSeenAiDisclosure: Boolean = false,
)
