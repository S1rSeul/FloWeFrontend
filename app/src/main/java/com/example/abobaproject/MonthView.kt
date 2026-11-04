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
    // Получаем первый день месяца и количество дней
    val calendar = remember(currentMonth.timeInMillis) {
        (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

    // ОПТИМИЗАЦИЯ: Группируем события по дням ОДИН раз при изменении списка items
    val itemsByDay = remember(items) {
        val map = mutableMapOf<String, List<CalendarItemDto>>()
        val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        items.forEach { item ->
            try {
                // Берем первые 19 символов для парсинга (yyyy-MM-dd'T'HH:mm:ss)
                val date = dateParser.parse(item.startsAt.take(19))
                if (date != null) {
                    val itemCal = Calendar.getInstance().apply { time = date }
                    // Ключ формата "yyyy-MM-dd" (например, "2026-11-15")
                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(itemCal.time)
                    map[key] = map.getOrDefault(key, emptyList()) + item
                }
            } catch (_: Exception) { }
        }
        map
    }

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

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Пустые ячейки для сдвига
            items(offset) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            // 2. Сами дни месяца
            items(daysInMonth) { index ->
                val dayNumber = index + 1
                val dayCalendar = (calendar.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, dayNumber)
                }

                // Формируем ключ для быстрого поиска (например, "2026-11-01")
                val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCalendar.time)
                val dayItems = itemsByDay[dayKey] ?: emptyList()

                DayCell(
                    dayNumber = dayNumber,
                    hasItems = dayItems.isNotEmpty(),
                    isToday = isToday(dayCalendar),
                    onClick = { onDayClick(dayCalendar.timeInMillis) }
                )
            }
        }
    }
}

@Composable
fun DayCell(dayNumber: Int, hasItems: Boolean, isToday: Boolean, onClick: () -> Unit) {
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
                color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            if (hasItems) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
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