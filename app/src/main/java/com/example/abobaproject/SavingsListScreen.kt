package com.example.abobaproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.abobaproject.SavingsViewModel
import com.example.abobaproject.data.model.SavingsAccountResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsListScreen(
    viewModel: SavingsViewModel,
    onCreateClick: () -> Unit,
    onAccountClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Накопительные счета") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.titleLarge) // Замени на иконку ArrowBack
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "Создать счет")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                viewModel.errorMessage != null -> Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                viewModel.isEmpty -> Text(
                    text = "У вас пока нет общих счетов.\nНажмите +, чтобы создать.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.savingsAccounts, key = { it.id }) { account ->
                            SavingsAccountCard(account) { onAccountClick(account.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsAccountCard(account: SavingsAccountResponse, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(account.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("${account.currentBalance} / ${account.targetAmount} ${account.currency}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))
            // Вычисляем прогресс локально на случай, если бэкенд вернет не 0.0-1.0
            val progressFraction = if (account.targetAmount > 0)
                (account.currentBalance / account.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("${(progressFraction * 100).toInt()}%", modifier = Modifier.align(Alignment.End), style = MaterialTheme.typography.labelSmall)
        }
    }
}