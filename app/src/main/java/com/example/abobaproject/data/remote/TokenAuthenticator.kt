package com.example.abobaproject.data.remote

import com.example.abobaproject.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val tokenManager: TokenManager) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Этот метод вызывается ТОЛЬКО если сервер вернул ошибку 401 (токен просрочен)

        // 1. Проверяем, не пытались ли мы уже обновить токен (защита от бесконечного цикла)
        if (response.request.header("Authorization") == null) {
            return null // Если токена вообще не было, значит и обновлять нечего
        }

        // 2. Достаем refresh token
        val refreshToken = runBlocking {
            tokenManager.refreshTokenFlow.firstOrNull()
        } ?: return null // Если refresh токена нет, выкидываем пользователя на логин

        // 3. TODO: Здесь должен быть вызов API /auth/refresh с refreshToken
        // Для примера сделаем заглушку, которая просто очищает токены
        // В реальном проекте тут будет вызов retrofit-сервиса для обновления

        // Временное решение: очищаем токены, чтобы пользователь перелогинился
        runBlocking { tokenManager.clearTokens() }
        return null

        // В идеале код должен выглядеть так:
        // val newTokens = apiService.refreshToken("Bearer $refreshToken")
        // tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
        // return response.request.newBuilder()
        //     .header("Authorization", "Bearer ${newTokens.accessToken}")
        //     .build()
    }
}