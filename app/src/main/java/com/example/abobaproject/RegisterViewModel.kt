package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.local.TokenManager
import com.example.abobaproject.data.model.LoginRequest
import com.example.abobaproject.data.model.RegisterRequest
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch

// Наследуемся от AndroidViewModel, чтобы получить доступ к Application (Context)
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    // Инициализируем сеть и хранилище токенов
    private val api = NetworkModule.provideApiService(application)
    private val tokenManager = TokenManager(application)

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
        errorMessage = null
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
        errorMessage = null
    }

    fun onConfirmPasswordChange(newValue: String) {
        confirmPassword = newValue
        errorMessage = null
    }

    var isAuthSuccess by mutableStateOf(false)
        private set

    fun resetAuthState() {
        isAuthSuccess = false
        errorMessage = null
    }

    fun testAuthorizedRequest() {
        viewModelScope.launch {
            try {
                Log.d("SpaceViewModel", "Отправка GET /couples/me...")
                val response = api.getCoupleStatus()
                Log.d("SpaceViewModel", "УСПЕХ! Ответ: $response")
            } catch (e: Exception) {
                Log.e("SpaceViewModel", "Ошибка: ${e.message}", e)
                // Если 404 — это значит токен принят, но пары нет. Это тоже УСПЕХ для AND-07!
                if (e is retrofit2.HttpException && e.code() == 404) {
                    Log.d("SpaceViewModel", "Токен принят, пары нет (404). AND-07 работает!")
                }
            }
        }
    }

    fun onLoginClick() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d("AuthViewModel", "Отправка запроса на вход...")
                val response = api.login(LoginRequest(email, password))
                Log.d("AuthViewModel", "Успешный вход! Токен: ${response.accessToken}")

                // Сохраняем токены
                tokenManager.saveTokens(response.accessToken, response.refreshToken)

                isAuthSuccess = true

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Ошибка входа: ${e.message}", e)
                errorMessage = e.message ?: "Ошибка сети"
            } finally {
                isLoading = false
            }
        }
    }

    fun onRegisterClick() {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }

        if (!email.matches(Regex("^[^@\\s]+@[^@.\\s]+(?:\\.[^@.\\s]+)+$"))) {
            errorMessage = "Некорректный email (пример: user@mail.com)"
            return
        }

        if (password != confirmPassword) {
            errorMessage = "Пароли не совпадают"
            return
        }

        if (password.length < 8) {
            errorMessage = "Пароль должен быть не менее 8 символов"
            return
        }

        if (password.length > 72) {
            errorMessage = "Пароль слишком длинный (макс. 72 символа)"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d("AuthViewModel", "Отправка запроса на регистрацию...")
                // Временное имя "User", так как в UI нет поля для имени
                val response = api.register(RegisterRequest(email, password, displayName = "User"))
                Log.d("AuthViewModel", "Успешная регистрация! Токен: ${response.accessToken}")

                tokenManager.saveTokens(response.accessToken, response.refreshToken)

                isAuthSuccess = true

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Ошибка регистрации: ${e.message}", e)

                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("AuthViewModel", "ТЕЛО ОТВЕТА: $errorBody")  // <-- Эту строку ищем в логах
                    errorMessage = errorBody ?: "Ошибка ${e.code()}"
                } else {
                    errorMessage = e.message ?: "Ошибка сети"
                }
            } finally {
                isLoading = false
            }
        }
    }
}