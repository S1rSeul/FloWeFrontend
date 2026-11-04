package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.model.CalendarItemDto
import com.example.abobaproject.data.model.CreateEventRequestDto
import com.example.abobaproject.data.model.UpdateEventRequestDto
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class CalendarViewMode { MONTH, WEEK }

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val api = NetworkModule.provideApiService(application)

    var viewMode by mutableStateOf(CalendarViewMode.MONTH)
    var selectedDate by mutableStateOf(System.currentTimeMillis())
    var calendarItems by mutableStateOf<List<CalendarItemDto>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var currentMonth by mutableStateOf(Calendar.getInstance())
        private set

    fun addEvent(title: String, description: String, startsAtMillis: Long, endsAtMillis: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Форматируем даты для бэкенда (UTC)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")

                val request = CreateEventRequestDto(
                    title = title,
                    description = description.ifBlank { null },
                    startsAt = sdf.format(Date(startsAtMillis)),
                    endsAt = sdf.format(Date(endsAtMillis)),
                    isShared = true
                )

                api.createEvent(request)
                loadCalendarData() // Обновляем список после создания
                onSuccess()        // Возвращаемся назад
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Ошибка создания события", e)
                // Здесь можно добавить обработку ошибки, например, через errorMessage
            }
        }
    }

    fun toggleViewMode() {
        viewMode = if (viewMode == CalendarViewMode.MONTH) CalendarViewMode.WEEK else CalendarViewMode.MONTH
        loadCalendarData()
    }

    fun changeMonth(offset: Int) {
        val newCalendar = (currentMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, offset)
        }
        currentMonth = newCalendar
        selectedDate = newCalendar.timeInMillis // Обновляем выбранную дату для загрузки
        loadCalendarData()
    }

    fun changeWeek(offset: Int) {
        val newCalendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        newCalendar.add(Calendar.WEEK_OF_YEAR, offset)
        selectedDate = newCalendar.timeInMillis // Обновляем выбранную дату
        loadCalendarData()
    }

    fun loadCalendarData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }

                // 1. Фиксируем понедельник как первый день недели
                calendar.firstDayOfWeek = Calendar.MONDAY

                if (viewMode == CalendarViewMode.MONTH) {
                    // Устанавливаем на 1-е число месяца
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    // Откатываемся назад до понедельника
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                    }
                } else {
                    // Для недели: откатываемся до начала текущей недели (понедельника)
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                    }
                }

                // Обнуляем время для точного начала дня
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val fromMillis = calendar.timeInMillis

                // Вычисляем конец периода
                if (viewMode == CalendarViewMode.MONTH) {
                    // Переходим на следующий месяц
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    // Идем вперед до ближайшего понедельника (конец видимой сетки)
                    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                    }
                } else {
                    // Для недели: прибавляем 7 дней к началу
                    calendar.add(Calendar.DAY_OF_MONTH, 7)
                }

                // Отнимаем 1 миллисекунду, чтобы не захватывать следующий день
                val toMillis = calendar.timeInMillis - 1

                // Форматируем даты для отправки на сервер
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")

                val fromStr = sdf.format(Date(fromMillis))
                val toStr = sdf.format(Date(toMillis))

                Log.d("CalendarVM", "Запрос видимого диапазона: from=$fromStr, to=$toStr")

                val items = api.getCalendar(from = fromStr, to = toStr)
                calendarItems = items

                Log.d("CalendarVM", "Получено элементов: ${items.size}")
                items.forEach {
                    Log.d("CalendarVM", "Элемент: type=${it.type}, title=${it.title}, startsAt=${it.startsAt}")
                }

            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Ошибка загрузки календаря", e)
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    errorMessage = "Ошибка сервера: ${e.code()} $errorBody"
                } else {
                    errorMessage = e.message ?: "Ошибка сети"
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun getEventById(id: String): CalendarItemDto? {
        return calendarItems.find { it.id == id }
    }

    fun updateEvent(eventId: String, title: String, description: String, startsAt: Long, endsAt: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")

                val request = UpdateEventRequestDto(
                    title = title,
                    description = description,
                    startsAt = sdf.format(Date(startsAt)),
                    endsAt = sdf.format(Date(endsAt)),
                    isShared = true
                )

                api.updateEvent(eventId, request)
                loadCalendarData() // Обновляем список
                onSuccess()
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Ошибка обновления события", e)
            }
        }
    }

    // Надежный парсер дат (как в TaskMapper)
    private fun parseIsoToCalendar(isoString: String): Calendar? {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(isoString) ?: continue
                return Calendar.getInstance().apply { time = date }
            } catch (_: Exception) { }
        }
        return null
    }
}