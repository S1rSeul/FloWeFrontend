package com.example.abobaproject.data.model

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val displayName: String)
data class AuthResponse(val accessToken: String, val refreshToken: String, val user: User? = null, val expiresIn: Long? = null)
data class User(val id: String, val email: String, val name: String)

data class RefreshRequest(val refreshToken: String)