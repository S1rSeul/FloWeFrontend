package com.example.abobaproject.data.model

import com.google.gson.annotations.SerializedName

// Ответ GET /tasks — обёртка с пагинацией
data class TaskListResponse(
    val items: List<TaskResponse> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

// Ответ GET /tasks, GET /tasks/{id}, POST /tasks, PATCH /tasks/{id}
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerializedName("createdById") val createdById: String? = null,
    @SerializedName("assigneeId") val assigneeId: String? = null,
    val deadline: String? = null,             // ISO 8601: "2026-10-12T13:11:11.05Z"
    val status: String = "todo",              // "todo", "in_progress", "done"
    @SerializedName("completedAt") val completedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    @SerializedName("assigneeId") val assigneeId: String? = null,
    val deadline: String? = null              // ISO 8601
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    @SerializedName("assigneeId") val assigneeId: String? = null,
    val deadline: String? = null
)

data class UpdateTaskStatusRequest(val status: String)

// Ответ GET /couples/me/members
data class MemberResponse(
    val id: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("isMe") val isMe: Boolean
)