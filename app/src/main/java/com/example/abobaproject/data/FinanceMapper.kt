package com.example.abobaproject.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object FinanceMapper {
    // Перевод из UI на сервер
    fun categoryToServer(ruCategory: String): String {
        return when (ruCategory) {
            "Еда" -> "FOOD"
            "Транспорт" -> "TRANSPORT"
            "Коммуналка" -> "HOUSING"
            "Развлечения" -> "ENTERTAINMENT"
            "Здоровье" -> "HEALTH"
            "Покупки" -> "SHOPPING"
            "Накопления" -> "SAVINGS"
            else -> "OTHER"
        }
    }

    // Перевод с сервера в UI
    fun categoryToUi(serverCategory: String?): String {
        return when (serverCategory) {
            "FOOD" -> "Еда"
            "TRANSPORT" -> "Транспорт"
            "HOUSING" -> "Коммуналка"
            "ENTERTAINMENT" -> "Развлечения"
            "HEALTH" -> "Здоровье"
            "SHOPPING" -> "Покупки"
            "SAVINGS" -> "Накопления"
            else -> "Другое"
        }
    }

    // Перевод даты "2026-11-23" в ISO "2026-11-23T12:00:00Z"
    fun dateToIso(dateStr: String): String {
        return try {
            val localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            // Сервер ждет дату-время. Добавляем полдень (12:00) текущего часового пояса
            "${localDate}T12:00:00Z"
        } catch (e: Exception) {
            "${LocalDate.now()}T12:00:00Z"
        }
    }
}