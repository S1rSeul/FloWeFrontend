package com.example.abobaproject

import java.util.UUID

enum class TaskAssignee(val displayName: String) {
    ME("Я"),
    PARTNER("Партнёр"),
    BOTH("Вместе")
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val deadlineEpochMillis: Long? = null,
    val assignee: TaskAssignee = TaskAssignee.ME,
    val isCompleted: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
