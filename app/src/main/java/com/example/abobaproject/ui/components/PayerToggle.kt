package com.example.abobaproject.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class PayerType { ME, PARTNER, BOTH }

@Composable
fun PayerToggle(
    selectedPayer: PayerType,
    onPayerSelected: (PayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PayerType.values().forEach { payer ->
            val label = when (payer) {
                PayerType.ME -> "Я"
                PayerType.PARTNER -> "Партнёр"
                PayerType.BOTH -> "Пополам"
            }
            FilterChip(
                selected = selectedPayer == payer,
                onClick = { onPayerSelected(payer) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}