package com.example.abobaproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// AND-13: Экран ожидания подключения партнера по сгенерированному коду
@Composable
fun WaitingPartnerScreen(
    inviteCode: String,
    onPartnerConnected: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ваше пространство создано!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Поделитесь этим кодом с вашей второй половинкой:")
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = inviteCode,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Ожидание подключения партнёра...")

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка для ручной симуляции подключения партнёра (для отладки)
        Button(
            onClick = onPartnerConnected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Партнёр подключился (Имитация)")
        }
        Button(
            onClick = { viewModel.testAuthorizedRequest() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Тестовый авторизованный запрос")
        }
    }
}