package com.example.abobaproject

import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long,
    val assignee: TaskAssignee = TaskAssignee.ME,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)