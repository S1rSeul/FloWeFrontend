package com.example.abobaproject.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Расширение для Context, чтобы создать DataStore
val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    // Сохранить токены
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // Получить Access Token (Flow, чтобы подписываться на изменения)
    val accessTokenFlow: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }

    // Получить Refresh Token
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    // Очистить токены (при выходе из аккаунта - AND-10)
    suspend fun clearTokens() {
        context.dataStore.edit { it.clear() }
    }
}