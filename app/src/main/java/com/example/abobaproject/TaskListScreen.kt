package com.example.abobaproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.abobaproject.ui.components.StatusIndicator
import com.example.abobaproject.ui.components.TaskItem

enum class TaskFilter(val label: String) {
    ALL("Все"),
    ACTIVE("Активные"),
    COMPLETED("Выполненные")
}

// AND-22: Экран списка задач
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    taskViewModel: TaskViewModel,
    onCreateTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSavingsClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onExpensesClick: () -> Unit = {},
    onBudgetsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentFilter by remember { mutableStateOf(TaskFilter.ALL) }

    var menuExpanded by remember { mutableStateOf(false) }

    val filteredTasks = when (currentFilter) {
        TaskFilter.ALL -> taskViewModel.tasks
        TaskFilter.ACTIVE -> taskViewModel.tasks.filter { !it.isCompleted }
        TaskFilter.COMPLETED -> taskViewModel.tasks.filter { it.isCompleted }
    }

    LaunchedEffect(Unit) {
        taskViewModel.refresh()
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать задачу"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Задачи FloWe",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    StatusIndicator(
                        isOnline = taskViewModel.isPartnerOnline,
                        onClick = { taskViewModel.togglePartnerStatus() }
                    )

                    // Кнопка вызова меню
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Финансы")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Накопительные счета") },
                                leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSavingsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("История расходов") },
                                leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onExpensesClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Бюджеты и лимиты") },
                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onBudgetsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Сводка расходов") },
                                leadingIcon = { Icon(Icons.Default.PieChart, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSummaryClick()
                                }
                            )
                        }
                    }

                    IconButton(onClick = onCalendarClick) {
                        Icon(Icons.Default.DateRange, contentDescription = "Календарь")
                    }

                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выйти")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TaskFilter.entries) { filter ->
                    FilterChip(
                        selected = currentFilter == filter,
                        onClick = { currentFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (taskViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (filteredTasks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Задач пока нет",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите +, чтобы добавить первую задачу",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItem(
                            task = task,
                            onToggleCompleted = { taskViewModel.toggleTaskCompleted(task.id) },
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }
        }
    }
}
