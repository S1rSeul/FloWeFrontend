package com.example.abobaproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.abobaproject.data.FinanceMapper
import com.example.abobaproject.data.model.BudgetResponse

// M-04-FE-01: Экран бюджетов и установки лимитов
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    budgetViewModel: BudgetViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBudgetForEdit by remember { mutableStateOf<BudgetResponse?>(null) }
    var showNewBudgetDialog by remember { mutableStateOf(false) }

    // АВТООБНОВЛЕНИЕ: Загружаем данные при каждом открытии экрана
    LaunchedEffect(Unit) {
        budgetViewModel.loadBudgets()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Бюджеты и лимиты") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewBudgetDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Установить лимит"
                )
            }
        }
    ) { paddingValues ->

        // SWIPE-TO-REFRESH: Оборачиваем весь контент в PullToRefreshBox
        PullToRefreshBox(
            isRefreshing = budgetViewModel.isLoading,
            onRefresh = { budgetViewModel.loadBudgets() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (budgetViewModel.isLoading && budgetViewModel.budgets.isEmpty()) {
                // Показываем загрузку только при первой загрузке (если список пуст)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (budgetViewModel.budgets.isEmpty()) {
                // Пустое состояние (добавлен verticalScroll, чтобы свайп работал даже когда список пуст)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Бюджеты не установлены",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showNewBudgetDialog = true }) {
                        Text("Установить лимит расхода")
                    }
                }
            } else {
                // Список бюджетов
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(budgetViewModel.budgets, key = { it.id }) { budget ->
                        BudgetCardItem(
                            budget = budget,
                            onClick = { selectedBudgetForEdit = budget }
                        )
                    }
                }
            }
        }
    }

    if (showNewBudgetDialog) {
        SetBudgetDialog(
            initialBudget = null,
            budgetViewModel = budgetViewModel,
            onDismiss = { showNewBudgetDialog = false }
        )
    }

    selectedBudgetForEdit?.let { budget ->
        SetBudgetDialog(
            initialBudget = budget,
            budgetViewModel = budgetViewModel,
            onDismiss = { selectedBudgetForEdit = null }
        )
    }
}

@Composable
fun BudgetCardItem(
    budget: BudgetResponse,
    onClick: () -> Unit
) {
    val progress = (budget.usagePercent / 100.0).toFloat().coerceIn(0f, 1f)
    val isExceeded = budget.isExceeded
    val isWarning = !isExceeded && progress >= 0.8f

    val progressColor = when {
        isExceeded -> Color(0xFFD32F2F)
        isWarning -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.primary
    }

    val uiCategory = budget.category?.let { FinanceMapper.categoryToUi(it) } ?: "Общий"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = uiCategory, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${budget.spentAmount.toInt()} / ${budget.limitAmount.toInt()} ${budget.currency}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isExceeded) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            if (isExceeded) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFEBEE)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Превышение на ${(budget.spentAmount - budget.limitAmount).toInt()} ${budget.currency}!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    initialBudget: BudgetResponse?,
    budgetViewModel: BudgetViewModel,
    onDismiss: () -> Unit
) {
    val categories = listOf("Общий", "Еда", "Транспорт", "Развлечения", "Коммуналка", "Покупки", "Другое")

    var selectedCategory by remember {
        mutableStateOf(
            initialBudget?.category?.let { FinanceMapper.categoryToUi(it) } ?: "Общий"
        )
    }
    var limitStr by remember { mutableStateOf(initialBudget?.limitAmount?.toInt()?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val isEditing = initialBudget != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Изменить лимит бюджета" else "Установка лимита бюджета") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Категория") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Категория") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Лимит суммы (₽)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                budgetViewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    budgetViewModel.saveBudget(
                        budgetId = initialBudget?.id,
                        category = selectedCategory,
                        limitAmountStr = limitStr,
                        currency = "RUB",
                        onSuccess = onDismiss
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isEditing) {
                    OutlinedButton(
                        onClick = {
                            budgetViewModel.deleteBudget(initialBudget!!.id)
                            onDismiss()
                        }
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}