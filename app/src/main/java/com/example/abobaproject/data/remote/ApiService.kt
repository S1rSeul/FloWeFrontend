package com.example.abobaproject.data.remote

import com.example.abobaproject.data.model.CreateCoupleRequest
import com.example.abobaproject.data.model.JoinCoupleRequest
import com.example.abobaproject.data.model.AuthResponse
import com.example.abobaproject.data.model.CoupleResponse
import com.example.abobaproject.data.model.LoginRequest
import com.example.abobaproject.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout()

    @GET("couples/me")
    suspend fun getCoupleStatus(): CoupleResponse

    @POST("couples")
    suspend fun createCouple(@Body request: CreateCoupleRequest): CoupleResponse

    @POST("couples/join")
    suspend fun joinCouple(@Body request: JoinCoupleRequest): CoupleResponse
}