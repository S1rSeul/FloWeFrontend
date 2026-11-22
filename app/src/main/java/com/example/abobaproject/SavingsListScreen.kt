package com.example.abobaproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

        PullToRefreshBox(
            isRefreshing = viewModel.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                // Ошибка
                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ошибка: ${viewModel.errorMessage}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.clearError()
                            viewModel.refresh()
                        }) {
                            Text("Повторить")
                        }
                    }
                }
                // Пустое состояние
                viewModel.isEmpty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "У вас пока нет общих счетов.\nНажмите +, чтобы создать.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Список счетов
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
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