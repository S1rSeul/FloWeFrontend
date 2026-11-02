package com.example.abobaproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onEventCreated: (title: String, description: String, startsAt: Long, endsAt: Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<Long?>(null) }
    var endTime by remember { mutableStateOf<Long?>(null) }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Новое событие") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                onValueChange = { title = it; if (it.isNotBlank()) isError = false },
                label = { Text("Название события *") },
                isError = isError,
                supportingText = if (isError) { { Text("Название не может быть пустым") } } else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Время начала:", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = {
                    showEventDateTimePicker(context) { startTime = it }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                val text = startTime?.let {
                    SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(it)
                } ?: "Выбрать время начала"
                Text(text)
            }

            Text("Время окончания:", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = {
                    // Инициализируем пикер временем начала, если оно уже выбрано
                    showEventDateTimePicker(context, startTime) { endTime = it }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                val text = endTime?.let {
                    SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(it)
                } ?: "Выбрать время окончания"
                Text(text)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else if (startTime == null || endTime == null) {
                        // Можно добавить Snackbar с ошибкой, но пока просто игнорируем
                    } else if (endTime!! <= startTime!!) {
                        // Валидация: конец должен быть позже начала
                    } else {
                        onEventCreated(title, description, startTime!!, endTime!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать событие")
            }
        }
    }
}

// Копия функции showDateTimePicker, чтобы не менять файл CreateTaskScreen
fun showEventDateTimePicker(
    context: Context,
    initialMillis: Long? = null,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    if (initialMillis != null) calendar.timeInMillis = initialMillis

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