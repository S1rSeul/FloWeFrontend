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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.abobaproject.data.model.CalendarItemDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                    Spacer(modifier = Modifier.height(2.dp))

                    // Используем "пирог" в шапке
                    val dayItemsHeader = items.filter { isItemOnDay(it, day) }
                    CalendarDayPie(
                        dayNumber = dayNumber,
                        items = dayItemsHeader,
                        isToday = isToday(day),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                    val dayItems = items.filter { isItemOnDay(it, day) }

                    dayItems.forEach { item ->
                        val originalStartCal = parseIsoDate(item.startsAt)
                        val originalEndCal = if (item.endsAt != null) parseIsoDate(item.endsAt) else null

                        if (originalStartCal != null) {
                            var startCal: Calendar
                            var endCal: Calendar

                            // Логика для задач (дедлайн - 1 час) и событий
                            if (item.type == "task_deadline") {
                                endCal = originalStartCal
                                startCal = (originalStartCal.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -1) }
                            } else {
                                startCal = originalStartCal
                                endCal = originalEndCal ?: (originalStartCal.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, 1) }
                            }

                            // ОБРЕЗАЕМ БЛОК ПО ГРАНИЦАМ ТЕКУЩЕГО ДНЯ
                            val dayStart = (day.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                            }
                            val dayEnd = (dayStart.clone() as Calendar).apply {
                                add(Calendar.DAY_OF_MONTH, 1)
                                add(Calendar.MILLISECOND, -1)
                            }

                            // Ищем пересечение времени события с текущим днем
                            val renderStartMillis = maxOf(startCal.timeInMillis, dayStart.timeInMillis)
                            val renderEndMillis = minOf(endCal.timeInMillis, dayEnd.timeInMillis)

                            // Если блок валиден (начало раньше конца)
                            if (renderStartMillis < renderEndMillis) {
                                val renderStartCal = Calendar.getInstance().apply { timeInMillis = renderStartMillis }
                                val renderEndCal = Calendar.getInstance().apply { timeInMillis = renderEndMillis }

                                val startHour = renderStartCal.get(Calendar.HOUR_OF_DAY)
                                val startMinute = renderStartCal.get(Calendar.MINUTE)

                                // Длительность в минутах (минимум 15 минут)
                                val durationMinutes = ((renderEndMillis - renderStartMillis) / (1000 * 60)).toInt().coerceAtLeast(15)
                                val minuteHeight = hourHeight / 60f

                                // Вычисляем отступ сверху и высоту блока (в Float, которые потом станут Dp)
                                val yOffset = (startHour * hourHeight.value) + (startMinute * minuteHeight.value)
                                val eventHeight = durationMinutes * minuteHeight.value

                                val bgColor = getCalendarItemContainerColor(item)
                                val txtColor = getCalendarItemTextColor(item)

                                // Блок события
                                Box(
                                    modifier = Modifier
                                        .offset(x = 2.dp, y = yOffset.dp)
                                        .fillMaxWidth()
                                        .height(eventHeight.dp)
                                        .padding(end = 2.dp, bottom = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(bgColor)
                                        .clickable {
                                            if (item.type == "event") {
                                                onEventClick(item.id)
                                            } else {
                                                onTaskClick(item.id)
                                            }
                                        }
                                        .zIndex(1f)
                                ) {
                                    val density = LocalDensity.current
                                    val scrollOffsetPx = scrollState.value
                                    val eventTopPx = with(density) { yOffset.dp.toPx() }

                                    // Вычисляем сдвиг, если мы прокрутили дальше начала блока
                                    val stickOffset = if (scrollOffsetPx > eventTopPx) {
                                        val shiftPx = scrollOffsetPx - eventTopPx
                                        // Ограничиваем сдвиг, чтобы текст не улетел за нижнюю границу блока
                                        val maxShiftPx = with(density) { (eventHeight - 40f).dp.toPx() }.coerceAtLeast(0f)
                                        with(density) { shiftPx.coerceAtMost(maxShiftPx).toDp() }
                                    } else {
                                        0.dp
                                    }

                                    Column(
                                        modifier = Modifier
                                            // Применяем динамический сдвиг к тексту
                                            .offset(y = stickOffset)
                                            .padding(4.dp)
                                    ) {
                                        // Название задачи/события
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = txtColor,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Время
                                        if (eventHeight > 40f) {
                                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                            Text(
                                                text = "${timeFormat.format(renderStartCal.time)} - ${timeFormat.format(renderEndCal.time)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = txtColor.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}