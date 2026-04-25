package com.xinyu.ai.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.xinyu.ai.domain.model.AppSettings
import com.xinyu.ai.domain.model.AppThemeMode
import com.xinyu.ai.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {
    override val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            authToken = preferences[AUTH_TOKEN],
            refreshToken = preferences[REFRESH_TOKEN],
            themeMode = AppThemeMode.valueOf(preferences[THEME_MODE] ?: AppThemeMode.SYSTEM.name),
            globalMemoryEnabled = preferences[GLOBAL_MEMORY_ENABLED] ?: true,
            hasSeenAiDisclosure = preferences[AI_DISCLOSURE_ACCEPTED] ?: false,
        )
    }

    override suspend fun setAuthToken(token: String?) {
        dataStore.edit { preferences ->
            if (token.isNullOrBlank()) {
                preferences.remove(AUTH_TOKEN)
            } else {
                preferences[AUTH_TOKEN] = token
            }
        }
    }

    override suspend fun setRefreshToken(token: String?) {
        dataStore.edit { preferences ->
            if (token.isNullOrBlank()) {
                preferences.remove(REFRESH_TOKEN)
            } else {
                preferences[REFRESH_TOKEN] = token
            }
        }
    }

    override suspend fun saveAuthSession(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun clearAuthSession() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    override suspend fun setGlobalMemoryEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[GLOBAL_MEMORY_ENABLED] = enabled
        }
    }

    override suspend fun setAiDisclosureAccepted(accepted: Boolean) {
        dataStore.edit { preferences ->
            preferences[AI_DISCLOSURE_ACCEPTED] = accepted
        }
    }

    private companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GLOBAL_MEMORY_ENABLED = booleanPreferencesKey("global_memory_enabled")
        val AI_DISCLOSURE_ACCEPTED = booleanPreferencesKey("ai_disclosure_accepted")
    }
}
