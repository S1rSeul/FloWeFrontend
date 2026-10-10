package com.example.abobaproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import java.util.Calendar

enum class QuickDeadline(val label: String, val epochOffsetMillis: Long?) {
    NONE("Без дедлайна", null),
    TODAY("Сегодня", 24 * 60 * 60 * 1000L),
    TOMORROW("Завтра", 2 * 24 * 60 * 60 * 1000L),
    WEEK("Через неделю", 7 * 24 * 60 * 60 * 1000L)
}

fun showDateTimePicker(
    context: Context,
    initialMillis: Long? = null,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    if (initialMillis != null) {
        calendar.timeInMillis = initialMillis
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

// AND-23: Экран создания задачи
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onTaskCreated: (title: String, description: String, deadlineMillis: Long?, assignee: TaskAssignee) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedAssignee by remember { mutableStateOf(TaskAssignee.ME) }
    var selectedQuickDeadline by remember { mutableStateOf(QuickDeadline.NONE) }
    var customDeadlineMillis by remember { mutableStateOf<Long?>(null) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Новая задача") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
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
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) isError = false
                },
                label = { Text("Название задачи *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = isError,
                supportingText = if (isError) {
                    { Text("Название не может быть пустым") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
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
                        showDateTimePicker(context, customDeadlineMillis ?: System.currentTimeMillis()) { selectedMillis ->
                            customDeadlineMillis = selectedMillis
                        }
                    }
                ) {
                    Text("📅 Точные дата и время")
                }

                if (customDeadlineMillis != null) {
                    TextButton(onClick = { customDeadlineMillis = null }) {
                        Text("Сбросить")
                    }
                }
            }

            if (customDeadlineMillis != null) {
                DeadlineBadge(deadlineEpochMillis = customDeadlineMillis)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QuickDeadline.entries) { deadline ->
                        FilterChip(
                            selected = selectedQuickDeadline == deadline,
                            onClick = { selectedQuickDeadline = deadline },
                            label = { Text(deadline.label) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        val deadlineMillis = customDeadlineMillis ?: selectedQuickDeadline.epochOffsetMillis?.let {
                            System.currentTimeMillis() + it
                        }
                        onTaskCreated(title, description, deadlineMillis, selectedAssignee)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать задачу")
            }
        }
    }
}
