package com.example.abobaproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditSavingsScreen(
    onSave: (name: String, targetAmount: Double, currency: String) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("RUB") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый счет") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.titleLarge) // Замени на иконку
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorText = null },
                label = { Text("Название цели (например, 'Отпуск')") },
                modifier = Modifier.fillMaxWidth(),
                isError = errorText != null
            )

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it; errorText = null },
                label = { Text("Целевая сумма") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = errorText != null
            )

            // Выбор валюты (упрощенный)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("RUB", "USD", "EUR").forEach { curr ->
                    FilterChip(
                        selected = currency == curr,
                        onClick = { currency = curr },
                        label = { Text(curr) }
                    )
                }
            }

            if (errorText != null) {
                Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorText = "Введите название"
                    } else if (amount == null || amount <= 0.0) {
                        errorText = "Введите корректную сумму"
                    } else {
                        onSave(name, amount, currency)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}