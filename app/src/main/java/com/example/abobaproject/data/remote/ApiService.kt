package com.example.abobaproject.data.remote

import com.example.abobaproject.data.model.AuthResponse
import com.example.abobaproject.data.model.LoginRequest
import com.example.abobaproject.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}