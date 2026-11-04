package com.example.abobaproject

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.example.abobaproject.data.model.CalendarItemDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// 1. Проверяем, пересекается ли событие с конкретным днем
fun isItemOnDay(item: CalendarItemDto, day: Calendar): Boolean {
    val startCal = parseIsoDate(item.startsAt) ?: return false

    val endCal = if (item.endsAt != null) {
        parseIsoDate(item.endsAt) ?: startCal
    } else {
        startCal
    }

    // Границы текущего дня (00:00:00.000 - 23:59:59.999)
    val dayStart = (day.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1

    val itemStart = startCal.timeInMillis
    val itemEnd = endCal.timeInMillis

    // Пересекаются, если начало события до конца дня, а конец события после начала дня
    return itemStart <= dayEnd && itemEnd >= dayStart
}

// 2. Универсальный компонент "Пирог" для дня
@Composable
fun CalendarDayPie(
    dayNumber: Int?,
    items: List<CalendarItemDto>,
    isToday: Boolean,
    isCurrentMonth: Boolean = true,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val segmentColors = remember(items) {
        var hasEvent = false
        var hasOverdue = false
        var hasDone = false
        var hasActive = false

        items.forEach { item ->
            when {
                item.type == "event" -> hasEvent = true
                item.isOverdue -> hasOverdue = true
                item.status == "done" -> hasDone = true
                else -> hasActive = true
            }
        }

        val colors = mutableListOf<Color>()
        if (hasEvent) colors.add(EventBlue)
        if (hasOverdue) colors.add(OverdueRed)
        if (hasDone) colors.add(DoneGreen)
        if (hasActive) colors.add(ActiveYellow)
        colors
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // Рисуем сектора
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (segmentColors.isNotEmpty()) {
                val sweepAngle = 360f / segmentColors.size
                var startAngle = -90f
                segmentColors.forEach { color ->
                    drawArc(
                        color = color.copy(alpha = 0.4f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = this.size
                    )
                    startAngle += sweepAngle
                }
            }
        }

        if (dayNumber != null) {
            Text(
                text = dayNumber.toString(),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
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