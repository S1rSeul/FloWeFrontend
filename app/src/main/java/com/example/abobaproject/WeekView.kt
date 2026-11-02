package com.example.abobaproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.abobaproject.data.model.CalendarItemDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun WeekView(
    selectedDate: Long,
    items: List<CalendarItemDto>,
    onEventClick: (String) -> Unit,
    onTaskClick: (String) -> Unit
) {
    val hourHeight = 60.dp
    val totalHeight = hourHeight * 24

    // Начало выбранной недели (Понедельник)
    val startOfWeek = remember(selectedDate) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
    }

    // Парсер времени
    val timeParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // 7 дней недели
    val weekDays = remember(startOfWeek) {
        List(7) { dayOffset ->
            (startOfWeek.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, dayOffset) }
        }
    }

    val scrollState = rememberScrollState()

    // Автоскролл на 8 утра при открытии
    LaunchedEffect(Unit) {
        scrollState.scrollTo((8 * hourHeight.value).toInt())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовки дней недели
        Row(modifier = Modifier.fillMaxWidth().padding(start = 50.dp)) {
            weekDays.forEach { day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dayName = SimpleDateFormat("EEE", Locale("ru")).format(day.time)
                    val dayNumber = day.get(Calendar.DAY_OF_MONTH)
                    Text(text = dayName, style = MaterialTheme.typography.labelSmall)
                    Text(text = dayNumber.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        // Основная сетка (скроллится вертикально)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Колонка с часами (слева)
            Column(modifier = Modifier.width(50.dp)) {
                for (hour in 0..23) {
                    Box(
                        modifier = Modifier.height(hourHeight).fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = String.format("%02d:00", hour),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.offset(y = (-8).dp)
                        )
                    }
                }
            }

            // Колонки с днями
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(totalHeight)
                ) {
                    // 1. Рисуем фоновые линии сетки
                    Column {
                        for (hour in 0..23) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(hourHeight)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                            }
                        }
                    }

                    // 2. Отрисовываем события
                    val dayItems = items.filter { item ->
                        val startDate = try { timeParser.parse(item.startsAt.take(19)) } catch (e: Exception) { null }
                        if (startDate != null) {
                            val itemCal = Calendar.getInstance().apply { time = startDate }
                            itemCal.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH) &&
                                    itemCal.get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
                                    itemCal.get(Calendar.YEAR) == day.get(Calendar.YEAR)
                        } else false
                    }

                    dayItems.forEach { item ->
                        val startCal = Calendar.getInstance().apply { time = timeParser.parse(item.startsAt.take(19))!! }

                        // Если endsAt нет, считаем что событие длится 1 час
                        val endCal = if (item.endsAt != null) {
                            Calendar.getInstance().apply { time = timeParser.parse(item.endsAt.take(19))!! }
                        } else {
                            (startCal.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 1) }
                        }

                        val startHour = startCal.get(Calendar.HOUR_OF_DAY)
                        val startMinute = startCal.get(Calendar.MINUTE)

                        // Длительность в минутах (минимум 30 минут, чтобы блок был виден)
                        val durationMinutes = ((endCal.timeInMillis - startCal.timeInMillis) / (1000 * 60)).toInt().coerceAtLeast(30)

                        val minuteHeight = hourHeight / 60f

                        // Вычисляем отступ сверху и высоту блока
                        val yOffset = (startHour * hourHeight.value) + (startMinute * minuteHeight.value)
                        val eventHeight = durationMinutes * minuteHeight.value

                        val isEvent = item.type == "event"
                        val color = if (isEvent) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer

                        // Блок события
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = yOffset.dp)
                                .fillMaxWidth()
                                .height(eventHeight.dp)
                                .padding(end = 2.dp, bottom = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                                .clickable {
                                    if (item.type == "event") {
                                        onEventClick(item.id)
                                    } else {
                                        onTaskClick(item.id) // Если это дедлайн задачи
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}