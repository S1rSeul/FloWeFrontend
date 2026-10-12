package com.example.abobaproject.data.remote

import com.example.abobaproject.data.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("tasks")
    suspend fun getTasks(
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null
    ): TaskListResponse

    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): TaskResponse

    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: String): TaskResponse

    @PATCH("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body request: UpdateTaskRequest
    ): TaskResponse

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String)

    @PATCH("tasks/{id}/status")
    suspend fun updateTaskStatus(
        @Path("id") id: String,
        @Body request: UpdateTaskStatusRequest
    ): TaskResponse

    @GET("couples/me/members")
    suspend fun getMembers(): List<MemberResponse>
}