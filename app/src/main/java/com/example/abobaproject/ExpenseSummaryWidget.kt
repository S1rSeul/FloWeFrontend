package com.example.abobaproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.abobaproject.SummaryViewModel
import androidx.compose.runtime.*

@Composable
fun ExpenseSummaryWidget(viewModel: SummaryViewModel) {
    LaunchedEffect(Unit) { viewModel.loadSummary() }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Вклад в общие расходы (30 дней)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else if (viewModel.summary?.summaries?.isEmpty() == true) {
                Text("Нет данных о расходах", style = MaterialTheme.typography.bodyMedium)
            } else {
                viewModel.summary?.summaries?.forEach { summary ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Вы: ${summary.myPaidAmount} ${summary.currency}", style = MaterialTheme.typography.bodyMedium)
                        Text("Партнёр: ${summary.partnerPaidAmount} ${summary.currency}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}