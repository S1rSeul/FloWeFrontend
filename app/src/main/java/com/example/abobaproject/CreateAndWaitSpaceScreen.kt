package com.example.abobaproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.abobaproject.ui.components.QrCodeImage

@Composable
fun WaitingPartnerScreen(
    inviteCode: String,
    spaceViewModel: SpaceViewModel,
    onPartnerConnected: () -> Unit,
    onLogoutClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        spaceViewModel.startPollingForPartner(onPartnerConnected)
    }

    DisposableEffect(Unit) {
        onDispose { spaceViewModel.stopPolling() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ваше пространство создано!", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Text("Поделитесь этим кодом с вашей второй половинкой:")
        Spacer(Modifier.height(8.dp))

        Text(
            text = inviteCode,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        QrCodeImage(
            content = inviteCode,
            modifier = Modifier.size(220.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Или попросите партнёра отсканировать QR-код",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Ожидание подключения партнёра...")

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Выйти из аккаунта") }
    }
}