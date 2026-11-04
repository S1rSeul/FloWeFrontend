package com.example.abobaproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.abobaproject.data.model.CalendarItemDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun MonthView(
    currentMonth: Calendar,
    items: List<CalendarItemDto>,
    onDayClick: (Long) -> Unit
) {
    // 1. Вычисляем ПОЛНЫЙ видимый диапазон (с понедельника первой недели по воскресенье последней)
    val gridDates = remember(currentMonth.timeInMillis) {
        val dates = mutableListOf<Calendar>()

        // Находим первый день месяца
        val firstDay = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Откатываемся назад до понедельника
        while (firstDay.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            firstDay.add(Calendar.DAY_OF_MONTH, -1)
        }

        // Находим последний день месяца
        val lastDay = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        // Идем вперед до воскресенья
        while (lastDay.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            lastDay.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Заполняем список дат
        val iter = firstDay.clone() as Calendar
        while (iter.timeInMillis <= lastDay.timeInMillis) {
            dates.add(iter.clone() as Calendar)
            iter.add(Calendar.DAY_OF_MONTH, 1)
        }
        dates
    }

    // Группируем события по дням
    val itemsByDay = remember(items) {
        val map = mutableMapOf<String, List<CalendarItemDto>>()
        val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        items.forEach { item ->
            try {
                val date = dateParser.parse(item.startsAt.take(19))
                if (date != null) {
                    val itemCal = Calendar.getInstance().apply { time = date }
                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(itemCal.time)
                    map[key] = map.getOrDefault(key, emptyList()) + item
                }
            } catch (_: Exception) { }
        }
        map
    }

    val currentMonthValue = currentMonth.get(Calendar.MONTH)

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовки дней недели
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Отрисовываем сплошную сетку
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gridDates) { date ->
                val dayNumber = date.get(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = date.get(Calendar.MONTH) == currentMonthValue

                val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.time)
                val dayItems = itemsByDay[dayKey] ?: emptyList()

                DayCell(
                    dayNumber = dayNumber,
                    hasItems = dayItems.isNotEmpty(),
                    isToday = isToday(date),
                    isCurrentMonth = isCurrentMonth, // 👈 Передаем флаг "своего" месяца
                    onClick = { onDayClick(date.timeInMillis) }
                )
            }
        }
    }
}

@Composable
fun DayCell(
    dayNumber: Int,
    hasItems: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean, // 👈 Новый параметр
    onClick: () -> Unit
) {
    // Если день не из текущего месяца - делаем его блеклым
    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (hasItems) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            // Точку тоже делаем блеклой для чужого месяца
                            if (isCurrentMonth) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}