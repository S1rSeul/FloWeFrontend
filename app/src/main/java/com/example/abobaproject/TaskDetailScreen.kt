package com.example.abobaproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.abobaproject.ui.components.DeadlineBadge

// AND-24: Экран деталей задачи
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val initialTask = remember(taskId) { taskViewModel.getTaskById(taskId) }

    if (initialTask == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Задача не найдена или была удалена.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick) { Text("Назад") }
        }
        return
    }

    var title by remember { mutableStateOf(initialTask.title) }
    var description by remember { mutableStateOf(initialTask.description) }
    var selectedAssignee by remember { mutableStateOf(initialTask.assignee) }
    var isCompleted by remember { mutableStateOf(initialTask.isCompleted) }
    var deadlineEpochMillis by remember { mutableStateOf(initialTask.deadlineEpochMillis) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Детали задачи") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        taskViewModel.deleteTask(taskId)
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить задачу",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { isCompleted = it }
                )
                Text(
                    text = if (isCompleted) "Выполнена" else "В процессе",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название задачи") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                minLines = 3,
                maxLines = 5,
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Исполнитель:",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskAssignee.entries.forEach { assignee ->
                    FilterChip(
                        selected = selectedAssignee == assignee,
                        onClick = { selectedAssignee = assignee },
                        label = { Text(assignee.displayName) }
                    )
                }
            }

            Text(
                text = "Дедлайн (AND-30):",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showDateTimePicker(context, deadlineEpochMillis ?: System.currentTimeMillis()) { selectedMillis ->
                            deadlineEpochMillis = selectedMillis
                        }
                    }
                ) {
                    Text("📅 Выбрать точные дату и время")
                }

                if (deadlineEpochMillis != null) {
                    TextButton(onClick = { deadlineEpochMillis = null }) {
                        Text("Удалить дедлайн")
                    }
                }
            }

            if (deadlineEpochMillis != null) {
                DeadlineBadge(deadlineEpochMillis = deadlineEpochMillis)
            } else {
                Text(
                    text = "Без дедлайна",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    taskViewModel.updateTask(
                        taskId = taskId,
                        title = title,
                        description = description,
                        deadlineEpochMillis = deadlineEpochMillis,
                        assignee = selectedAssignee,
                        isCompleted = isCompleted
                    )
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить изменения")
            }

            OutlinedButton(
                onClick = {
                    taskViewModel.deleteTask(taskId)
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Удалить задачу",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
