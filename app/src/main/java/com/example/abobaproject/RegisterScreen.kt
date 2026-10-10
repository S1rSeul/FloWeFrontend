package com.example.abobaproject

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(viewModel: RegisterViewModel = viewModel()) {
    Column {
        Text(text = "Регистрация")

        Text(text = "Email")
        TextField(
            value = viewModel.email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = { Text("Введите email") }
        )

        Text(text = "Пароль")
        TextField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            placeholder = { Text("Введите пароль") },
            visualTransformation = PasswordVisualTransformation()
        )

        Text(text = "Подтвердите пароль")
        TextField(
            value = viewModel.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            placeholder = { Text("Повторите пароль") },
            visualTransformation = PasswordVisualTransformation()
        )

        // Отображение ошибки
        viewModel.errorMessage?.let { error ->
            Text(text = error)
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.onRegisterClick() }) {
                Text(text = "Зарегистрироваться")
            }
        }
    }
}
