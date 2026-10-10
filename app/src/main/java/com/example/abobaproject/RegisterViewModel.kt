package com.example.abobaproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

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

    fun onRegisterClick() {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }

        if (password != confirmPassword) {
            errorMessage = "Пароли не совпадают"
            return
        }

        if (password.length < 6) {
            errorMessage = "Пароль должен быть не менее 6 символов"
            return
        }

        // Отправка данных на сервер
        isLoading = true
        // TODO: Вызов сетевого запроса (Retrofit / Ktor)
    }
}