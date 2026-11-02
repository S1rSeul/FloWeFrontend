package com.example.abobaproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.abobaproject.data.model.CalendarItemDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: CalendarItemDto?, // Передаем само событие для заполнения полей
    onSaveClick: (title: String, description: String, startsAt: Long, endsAt: Long) -> Unit,
    onBackClick: () -> Unit
) {
    if (event == null) {
        Text("Событие не найдено")
        return
    }

    val context = LocalContext.current

    // Парсим даты из строки в Long
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf("") } // В DTO нет description, если он нужен - добавьте
    var startTime by remember { mutableStateOf(parser.parse(event.startsAt.take(19))?.time) }
    var endTime by remember { mutableStateOf(event.endsAt?.let { parser.parse(it.take(19))?.time }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название события") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Начало:")
            OutlinedButton(onClick = {
                showEditDateTimePicker(context, startTime) { startTime = it }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(startTime?.let { SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(it) } ?: "Выбрать")
            }

            Text("Конец:")
            OutlinedButton(onClick = {
                showEditDateTimePicker(context, endTime) { endTime = it }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(endTime?.let { SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(it) } ?: "Выбрать")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && startTime != null && endTime != null && endTime!! > startTime!!) {
                        onSaveClick(title, description, startTime!!, endTime!!)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить изменения")
            }
        }
    }
}

// Копия функции пикера
fun showEditDateTimePicker(context: android.content.Context, initialMillis: Long?, onDateTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { if (initialMillis != null) timeInMillis = initialMillis }
    DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d)
        TimePickerDialog(context, { _, h, min ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, min)
            onDateTimeSelected(calendar.timeInMillis)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}