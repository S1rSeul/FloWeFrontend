package com.example.abobaproject.data.model

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerializedName("createdById") val createdById: String? = null,
    @SerializedName("assignedToMe") val assignedToMe: Boolean = false,
    @SerializedName("assignedToPartner") val assignedToPartner: Boolean = false,
    @SerializedName("dueDate") val dueDate: String? = null,
    val status: String = "todo",
    @SerializedName("isOverdue") val isOverdue: Boolean = false,
    @SerializedName("completedAt") val completedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    @SerializedName("assignedToMe") val assignedToMe: Boolean,
    @SerializedName("assignedToPartner") val assignedToPartner: Boolean,
    @SerializedName("dueDate") val dueDate: String? = null
)

data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    @SerializedName("assignedToMe") val assignedToMe: Boolean? = null,
    @SerializedName("assignedToPartner") val assignedToPartner: Boolean? = null,
    @SerializedName("dueDate") val dueDate: String? = null
)

data class UpdateTaskStatusRequest(val status: String)

data class MemberResponse(
    val id: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("isMe") val isMe: Boolean
)

data class TaskListResponse(
    val items: List<TaskResponse> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)