package com.example.abobaproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NoSpaceScreen(
    spaceViewModel: SpaceViewModel,
    onSpaceReady: () -> Unit,       // вызывается когда пара создана/присоединена
    onScanQrClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var inputCode by remember { mutableStateOf("") }

    // Реагируем на смену состояния: как только пара появилась — уходим с экрана
    LaunchedEffect(spaceViewModel.spaceState) {
        val state = spaceViewModel.spaceState
        if (state is SpaceState.WaitingPartner || state is SpaceState.Active) {
            onSpaceReady()
        }
    }

    // Логирование состояния (временно, для отладки)
    LaunchedEffect(inputCode, spaceViewModel.isActionLoading) {
        android.util.Log.d("NoSpaceScreen",
            "inputCode='$inputCode' (isNotBlank=${inputCode.isNotBlank()}), " +
                    "isActionLoading=${spaceViewModel.isActionLoading}, " +
                    "state=${spaceViewModel.spaceState::class.simpleName}")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Добро пожаловать в FloWe!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Создайте общее пространство или присоединитесь по коду партнёра.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))

        if (spaceViewModel.isActionLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { spaceViewModel.createCouple() },
            enabled = !spaceViewModel.isActionLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Создать пространство") }

        Spacer(Modifier.height(24.dp))

        Text("Или введите код приглашения:")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it },
            label = { Text("Код партнёра") },
            enabled = !spaceViewModel.isActionLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { spaceViewModel.joinCouple(inputCode) },
            enabled = inputCode.isNotBlank() && !spaceViewModel.isActionLoading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Присоединиться") }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onScanQrClick,
            enabled = !spaceViewModel.isActionLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сканировать QR-код")
        }

        spaceViewModel.actionError?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(48.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Выйти из аккаунта") }
    }
}