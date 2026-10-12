package com.example.abobaproject.data.remote

import android.util.Log
import com.example.abobaproject.data.local.TokenManager
import com.example.abobaproject.data.model.RefreshRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val tokenManager: TokenManager) : Authenticator {

    // Отдельный клиент без интерцепторов — чтобы refresh не попал под наш же Authenticator
    private val refreshApi: RefreshApiService = RefreshApiService.create()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Защита от бесконечного цикла: если в исходном запросе не было Authorization,
        // значит проблема не в токене — повторять бессмысленно.
        if (response.request.header("Authorization") == null) {
            Log.w("TokenAuthenticator", "401 без Authorization — refresh не поможет")
            return null
        }

        // Синхронизация: чтобы параллельные 401 не делали несколько refresh-запросов
        synchronized(this) {
            return runBlocking {
                // 1. Достаём refresh-токен
                val refreshToken = tokenManager.refreshTokenFlow.firstOrNull()
                if (refreshToken.isNullOrBlank()) {
                    Log.e("TokenAuthenticator", "Refresh-токен отсутствует → logout")
                    return@runBlocking null
                }

                try {
                    Log.d("TokenAuthenticator", "Access-токен истёк, обновляем...")

                    // 2. Идём на /auth/refresh
                    val newTokens = refreshApi.refresh(RefreshRequest(refreshToken))

                    // 3. Сохраняем новые токены
                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                    Log.d("TokenAuthenticator", "Токены обновлены, повторяем исходный запрос")

                    // 4. Повторяем исходный запрос с новым access-токеном
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()

                } catch (e: Exception) {
                    Log.e("TokenAuthenticator", "Не удалось обновить токен: ${e.message}", e)
                    // Refresh не сработал — сбрасываем всё, пользователя надо на логин
                    tokenManager.clearTokens()
                    null
                }
            }
        }
    }
}