package com.example.abobaproject

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
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    event: CalendarItemDto?,
    onSaveClick: (title: String, description: String, startsAt: Long, endsAt: Long) -> Unit,
    onBackClick: () -> Unit
) {
    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Событие не найдено")
        }
        return
    }

    val context = LocalContext.current

    // Парсим даты из строки в Long
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description ?: "") } // Берем описание из DTO
    var startTime by remember { mutableStateOf(parser.parse(event.startsAt.take(19))?.time) }
    var endTime by remember { mutableStateOf(event.endsAt?.let { parser.parse(it.take(19))?.time }) }

    // Валидация
    var isTitleError by remember { mutableStateOf(false) }
    var isTimeError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование события") },
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
            // Поле Название
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; if (it.isNotBlank()) isTitleError = false },
                label = { Text("Название события *") },
                isError = isTitleError,
                supportingText = if (isTitleError) { { Text("Название не может быть пустым") } } else null,
                modifier = Modifier.fillMaxWidth()
            )

            // Поле Описание
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // Время начала
            Text("Время начала:", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = {
                    showEventDateTimePicker(context, startTime) {
                        startTime = it
                        isTimeError = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                val text = startTime?.let {
                    SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(it)
                } ?: "Выбрать время начала"
                Text(text)
            }

            // Время окончания
            Text("Время окончания:", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = {
                    showEventDateTimePicker(context, endTime ?: startTime) {
                        endTime = it
                        isTimeError = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                val text = endTime?.let {
                    SimpleDateFormat("dd MMMM, HH:mm", Locale("ru")).format(it)
                } ?: "Выбрать время окончания"
                Text(text)
            }

            if (isTimeError) {
                Text(
                    text = "Время окончания должно быть позже времени начала",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка сохранения
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                    } else if (startTime == null || endTime == null) {
                        isTimeError = true // Или можно вывести другой текст ошибки
                    } else if (endTime!! <= startTime!!) {
                        isTimeError = true
                    } else {
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