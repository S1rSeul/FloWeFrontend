package com.example.abobaproject.data.model

// Модель для получения данных из /api/v1/calendar
data class CalendarItemDto(
    val id: String,
    val type: String,
    val title: String,
    val startsAt: String,
    val endsAt: String? = null,
    val isMyPersonal: Boolean = false,
    val isPartnerPersonal: Boolean = false,
    val assignedToMe: Boolean = false,
    val assignedToPartner: Boolean = false,
    val isOverdue: Boolean = false,
    val status: String? = null
)

// Модель для создания события
data class CreateEventRequestDto(
    val title: String,
    val description: String? = null,
    val startsAt: String, // ISO 8601
    val endsAt: String? = null,
    val isShared: Boolean = true
)

// Модель ответа при создании события
data class EventResponseDto(
    val id: String,
    val title: String,
    val description: String?,
    val createdById: String,
    val startsAt: String,
    val endsAt: String?,
    val isShared: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class UpdateEventRequestDto(
    val title: String?,
    val description: String?,
    val startsAt: String?,
    val endsAt: String?,
    val isShared: Boolean?
)