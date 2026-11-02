package com.example.abobaproject

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onTaskClick: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onCreateEventClick: () -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(viewModel.selectedDate, viewModel.viewMode) {
        viewModel.loadCalendarData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад к задачам"
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Кнопка "Назад"
                        IconButton(onClick = {
                            if (viewModel.viewMode == CalendarViewMode.MONTH) viewModel.changeMonth(-1)
                            else viewModel.changeWeek(-1)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Назад")
                        }

                        // Текст: Месяц или Неделя
                        val titleText = if (viewModel.viewMode == CalendarViewMode.MONTH) {
                            SimpleDateFormat("LLLL yyyy", Locale("ru")).format(viewModel.currentMonth.time)
                        } else {
                            // Вычисляем начало и конец недели для заголовка
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = viewModel.selectedDate
                                // Устанавливаем день недели на понедельник
                                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            }
                            val start = SimpleDateFormat("dd", Locale("ru")).format(cal.time)
                            cal.add(Calendar.DAY_OF_YEAR, 6)
                            val end = SimpleDateFormat("dd MMM", Locale("ru")).format(cal.time)
                            "$start - $end"
                        }

                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Кнопка "Вперед"
                        IconButton(onClick = {
                            if (viewModel.viewMode == CalendarViewMode.MONTH) viewModel.changeMonth(1)
                            else viewModel.changeWeek(1)
                        }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Вперед")
                        }
                    }
                },
                actions = {
                    // Переключатель Месяц / Неделя
                    TextButton(onClick = { viewModel.toggleViewMode() }) {
                        Text(if (viewModel.viewMode == CalendarViewMode.MONTH) "Показать неделю" else "Показать месяц")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateEventClick) {
                Icon(Icons.Default.Add, contentDescription = "Новое событие")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.errorMessage != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ошибка: ${viewModel.errorMessage}")
                }
            } else {
                if (viewModel.viewMode == CalendarViewMode.MONTH) {
                    MonthView(
                        currentMonth = viewModel.currentMonth,
                        items = viewModel.calendarItems,
                        onDayClick = { /* Пока оставляем пустым */ }
                    )
                } else {
                    WeekView(
                        selectedDate = viewModel.selectedDate,
                        items = viewModel.calendarItems,
                        onEventClick = onEventClick,
                        onTaskClick = onTaskClick
                    )
                }
            }
        }
    }
}