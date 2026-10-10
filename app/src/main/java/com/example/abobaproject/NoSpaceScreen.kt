package com.example.abobaproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NoSpaceScreen(
    onCreateSpaceClick: () -> Unit,
    onJoinSpaceClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    var inputCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Добро пожаловать в FloWe!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Создайте общее пространство для вашей пары или присоединитесь по коду партнёра.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateSpaceClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Создать пространство")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Или введите код приглашения:")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it },
            label = { Text("Код партнёра") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onJoinSpaceClick(inputCode) },
            enabled = inputCode.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Присоединиться")
        }

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта")
        }
    }
}