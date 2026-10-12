package com.example.abobaproject.data

import com.example.abobaproject.Task
import com.example.abobaproject.TaskAssignee
import com.example.abobaproject.data.model.TaskResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TaskMapper {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",  // 6 миллисекунд
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",     // 3 миллисекунды
        "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",      // 2 миллисекунды
        "yyyy-MM-dd'T'HH:mm:ss.S'Z'",       // 1 миллисекунда
        "yyyy-MM-dd'T'HH:mm:ss'Z'"          // без миллисекунд
    )

    private fun parseIso(iso: String): Date? {
        for (pattern in patterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }
                return fmt.parse(iso)
            } catch (_: Exception) { /* пробуем следующий */ }
        }
        return null
    }

    fun isoToMillis(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        return parseIso(iso)?.time
    }

    fun millisToIso(millis: Long?): String? {
        if (millis == null) return null
        return isoFormat.format(Date(millis))
    }

    /**
     * Клиентская модель: assignee определяется по двум булевым полям.
     */
    fun toClientTask(resp: TaskResponse): Task {
        val assignee = when {
            resp.assignedToMe && resp.assignedToPartner -> TaskAssignee.BOTH
            resp.assignedToPartner -> TaskAssignee.PARTNER
            else -> TaskAssignee.ME
        }
        return Task(
            id = resp.id,
            title = resp.title,
            description = resp.description ?: "",
            deadlineEpochMillis = isoToMillis(resp.dueDate),
            assignee = assignee,
            isCompleted = resp.status == "done",
            createdAtEpochMillis = isoToMillis(resp.createdAt) ?: System.currentTimeMillis()
        )
    }

    /**
     * Обратный маппинг: TaskAssignee → две булевых.
     */
    fun assigneeToFlags(assignee: TaskAssignee): Pair<Boolean, Boolean> {
        return when (assignee) {
            TaskAssignee.ME -> true to false
            TaskAssignee.PARTNER -> false to true
            TaskAssignee.BOTH -> true to true
        }
    }
}