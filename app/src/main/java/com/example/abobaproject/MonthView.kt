package com.example.abobaproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.abobaproject.data.model.CalendarItemDto
import java.util.Calendar

@Composable
fun MonthView(
    currentMonth: Calendar,
    items: List<CalendarItemDto>,
    onDayClick: (Long) -> Unit
) {
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

    val currentMonthValue = currentMonth.get(Calendar.MONTH)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            days.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
            items(gridDates, key = { it.timeInMillis }) { date ->
                val dayNumber = date.get(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = date.get(Calendar.MONTH) == currentMonthValue

                // Берем ВСЕ задачи, которые пересекаются с этим днем (включая многодневные)
                val dayItems = items.filter { isItemOnDay(it, date) }

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
    CalendarDayPie(
        dayNumber = dayNumber,
        items = items,
        isToday = isToday,
        isCurrentMonth = isCurrentMonth,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable { onClick() }
    )
}

fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}