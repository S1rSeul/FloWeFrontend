package com.example.abobaproject.data.remote

import com.example.abobaproject.data.model.AuthResponse
import com.example.abobaproject.data.model.RefreshRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Отдельный Retrofit-клиент ТОЛЬКО для refresh.
 * Он НЕ использует AuthInterceptor и TokenAuthenticator, чтобы избежать циклической зависимости.
 */
interface RefreshApiService {

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    companion object {
        // Важно: должен совпадать с BASE_URL в NetworkModule
        private const val BASE_URL = "http://localhost:8080/api/v1/"

        fun create(): RefreshApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RefreshApiService::class.java)
        }
    }
}