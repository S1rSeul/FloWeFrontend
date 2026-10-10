package com.example.abobaproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.abobaproject.ui.components.StatusIndicator

// AND-21: Экран после подключения к пространству пары
@Composable
fun SpaceConnectedScreen(
    isPartnerOnline: Boolean,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вы подключены! 🎉",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Совместное пространство успешно создано. Теперь вы можете вести общий список дел и задач.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Статус подключения",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                StatusIndicator(isOnline = isPartnerOnline)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNavigateToTasks,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Перейти к задачам (TaskTracker)")
        }
    }
}
