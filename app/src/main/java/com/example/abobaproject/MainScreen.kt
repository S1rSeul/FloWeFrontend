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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Главный экран FloWe",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Вы успешно вошли в совместное пространство!")

        Spacer(modifier = Modifier.height(48.dp))

        // AND-10: Выход из аккаунта
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выйти из аккаунта")
        }
    }
}