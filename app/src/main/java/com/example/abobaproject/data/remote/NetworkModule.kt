package com.example.abobaproject.data.remote

import android.content.Context
import com.example.abobaproject.data.local.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // ВАЖНО: Для эмулятора Android localhost компьютера — это 10.0.2.2
    // Если у вас бэкенд на компьютере, оставьте этот адрес. Если на другом сервере — впишите его IP.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    fun provideApiService(context: Context): ApiService {
        // 1. Создаем TokenManager
        val tokenManager = TokenManager(context)

        // 2. Создаем OkHttpClient и подключаем к нему наши классы
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        // 3. Собираем Retrofit
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Передаем ему настроенный OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}