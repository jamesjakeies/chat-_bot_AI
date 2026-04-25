package com.xinyu.ai.domain.repository

import com.xinyu.ai.domain.model.AppSettings
import com.xinyu.ai.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val settings: Flow<AppSettings>

    suspend fun setAuthToken(token: String?)

    suspend fun setRefreshToken(token: String?)

    suspend fun saveAuthSession(accessToken: String, refreshToken: String)

    suspend fun clearAuthSession()

    suspend fun setThemeMode(mode: AppThemeMode)

    suspend fun setGlobalMemoryEnabled(enabled: Boolean)

    suspend fun setAiDisclosureAccepted(accepted: Boolean)
}
