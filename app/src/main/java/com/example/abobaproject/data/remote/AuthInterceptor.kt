package com.example.abobaproject.data.remote

import com.example.abobaproject.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. Берем оригинальный запрос, который хочет сделать приложение
        val originalRequest = chain.request()

        // 2. Достаем токен из хранилища (DataStore работает асинхронно, поэтому runBlocking)
        val token = runBlocking {
            tokenManager.accessTokenFlow.firstOrNull()
        }

        // 3. Если токен есть, добавляем его в заголовок Authorization
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest // Если токена нет, отправляем как есть (например, запрос на логин)
        }

        // 4. Отправляем запрос дальше на сервер
        return chain.proceed(newRequest)
    }
}