package com.example.abobaproject.data

import com.example.abobaproject.Task
import com.example.abobaproject.TaskAssignee
import com.example.abobaproject.data.model.MemberResponse
import com.example.abobaproject.data.model.TaskResponse
import java.text.SimpleDateFormat
import java.util.*

object TaskMapper {

    // ISO 8601 с миллисекундами и Z-таймзоной — как в примерах Swagger
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun isoToMillis(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        return try {
            isoFormat.parse(iso)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun millisToIso(millis: Long?): String? {
        if (millis == null) return null
        return isoFormat.format(Date(millis))
    }

    /**
     * Конвертация серверной задачи в клиентскую модель.
     * assigneeId сравнивается со списком членов пары для определения ME / PARTNER / BOTH.
     */
    fun toClientTask(
        resp: TaskResponse,
        members: List<MemberResponse>
    ): Task {
        val myMember = members.firstOrNull { it.isMe }
        val partnerMember = members.firstOrNull { !it.isMe }

        val assignee = when (resp.assigneeId) {
            null -> TaskAssignee.BOTH
            myMember?.id -> TaskAssignee.ME
            partnerMember?.id -> TaskAssignee.PARTNER
            else -> TaskAssignee.BOTH
        }

        return Task(
            id = resp.id,
            title = resp.title,
            description = resp.description ?: "",
            deadlineEpochMillis = isoToMillis(resp.deadline),
            assignee = assignee,
            isCompleted = resp.status == "done",
            createdAtEpochMillis = isoToMillis(resp.createdAt) ?: System.currentTimeMillis()
        )
    }

    /**
     * Обратная конвертация: выбор assigneeId по клиентскому enum.
     * BOTH → null (сервер трактует как «вместе»).
     */
    fun assigneeToId(
        assignee: TaskAssignee,
        members: List<MemberResponse>
    ): String? {
        return when (assignee) {
            TaskAssignee.ME -> members.firstOrNull { it.isMe }?.id
            TaskAssignee.PARTNER -> members.firstOrNull { !it.isMe }?.id
            TaskAssignee.BOTH -> null
        }
    }
}