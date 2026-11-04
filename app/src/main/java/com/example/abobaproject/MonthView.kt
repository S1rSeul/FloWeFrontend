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
        val firstDay = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        while (firstDay.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            firstDay.add(Calendar.DAY_OF_MONTH, -1)
        }
        val lastDay = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        while (lastDay.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            lastDay.add(Calendar.DAY_OF_MONTH, 1)
        }
        val iter = firstDay.clone() as Calendar
        while (iter.timeInMillis <= lastDay.timeInMillis) {
            dates.add(iter.clone() as Calendar)
            iter.add(Calendar.DAY_OF_MONTH, 1)
        }
        dates
    }

    // 2. Группируем задачи с помощью надежного парсера
    val itemsByDay = remember(items) {
        val map = mutableMapOf<Triple<Int, Int, Int>, List<CalendarItemDto>>()
        items.forEach { item ->
            val itemCal = parseIsoDate(item.startsAt)
            if (itemCal != null) {
                val key = Triple(itemCal.get(Calendar.YEAR), itemCal.get(Calendar.MONTH), itemCal.get(Calendar.DAY_OF_MONTH))
                map[key] = map.getOrDefault(key, emptyList()) + item
            }
        }
        map
    }

    val currentMonthValue = currentMonth.get(Calendar.MONTH)

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовки дней недели
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            days.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Сетка
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
            items(gridDates, key = { it.timeInMillis }) { date ->
                val dayNumber = date.get(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = date.get(Calendar.MONTH) == currentMonthValue

                val dayKey = Triple(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
                val dayItems = itemsByDay[dayKey] ?: emptyList()

                DayCell(
                    dayNumber = dayNumber,
                    items = dayItems,
                    isToday = isToday(date),
                    isCurrentMonth = isCurrentMonth,
                    onClick = { onDayClick(date.timeInMillis) }
                )
            }
        }
    }
}

@Composable
fun DayCell(dayNumber: Int, items: List<CalendarItemDto>, isToday: Boolean, isCurrentMonth: Boolean, onClick: () -> Unit) {
    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // Блеклый цвет для хвостов
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(color = if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dayNumber.toString(), fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = textColor, style = MaterialTheme.typography.bodyMedium)

            if (items.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    items.take(3).forEach { item ->
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(getCalendarItemDotColor(item)))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

fun parseIsoDate(isoString: String): Calendar? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(isoString)
            if (date != null) {
                return Calendar.getInstance().apply { time = date }
            }
        } catch (_: Exception) {
            // Если не получилось - идем к следующему формату
        }
    }
    return null
}