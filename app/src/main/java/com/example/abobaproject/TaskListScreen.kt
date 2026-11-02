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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
    modifier: Modifier = Modifier
) {
    var currentFilter by remember { mutableStateOf(TaskFilter.ALL) }

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
                //horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Задачи FloWe",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1 // Запрещаем переносить текст на новую строку
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp) // Убираем лишние отступы между кнопками
                ) {
                    // AND-19 Индикатор состояния
                    StatusIndicator(
                        isOnline = taskViewModel.isPartnerOnline,
                        onClick = { taskViewModel.togglePartnerStatus() }
                    )

                    // Кнопка перехода в Календарь
                    IconButton(onClick = onCalendarClick) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Календарь"
                        )
                    }

                    // Кнопка выхода из аккаунта
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выйти из аккаунта"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Фильтры списка задач
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

            // AND-34: Визуальные состояния
            PullToRefreshBox(
                isRefreshing = taskViewModel.isLoading,
                onRefresh = { taskViewModel.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Занимает всё оставшееся место
            ) {
                when {
                    // Загрузка (только когда список пустой)
                    taskViewModel.isLoading && taskViewModel.tasks.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // Ошибка сети/сервера
                    taskViewModel.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()) // Чтобы свайп работал
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ошибка: ${taskViewModel.errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                taskViewModel.clearError()
                                taskViewModel.refresh()
                            }) {
                                Text("Повторить")
                            }
                        }
                    }

                    // Пусто
                    filteredTasks.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()) // Чтобы свайп работал
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
                                text = "Потяните вниз для обновления или нажмите +",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Список задач
                    else -> {
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
    }
}