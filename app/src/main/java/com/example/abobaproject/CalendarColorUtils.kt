package com.example.abobaproject

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.abobaproject.data.model.CalendarItemDto

val EventBlue = Color(0xFF007AFF)       // Ярко-синий для встреч
val ActiveYellow = Color(0xFFFFCC00)    // Ярко-жёлтый для активных задач
val OverdueRed = Color(0xFFFF3B30)      // Ярко-красный (алый) для просроченных
val DoneGreen = Color(0xFF34C759)       // Салатовый/зелёный для выполненных

val DarkText = Color(0xFF1C1C1E)        // Темный текст для желтого фона
val WhiteText = Color.White             // Белый текст для синего, красного и зеленого

// Возвращает цвет контейнера для фона
@Composable
fun getCalendarItemContainerColor(item: CalendarItemDto): Color {
    return when {
        item.type == "event" -> EventBlue
        item.isOverdue -> OverdueRed
        item.status == "done" -> DoneGreen
        else -> ActiveYellow
    }
}

// Возвращает цвет для точек в месячном виде
@Composable
fun getCalendarItemDotColor(item: CalendarItemDto): Color {
    return when {
        item.type == "event" -> EventBlue
        item.isOverdue -> OverdueRed
        item.status == "done" -> DoneGreen
        else -> ActiveYellow
    }
}

// Возвращает цвет текста для блоков в недельном виде
@Composable
fun getCalendarItemTextColor(item: CalendarItemDto): Color {
    return when {
        item.type == "event" -> DarkText
        item.isOverdue -> DarkText
        item.status == "done" -> DarkText
        else -> DarkText
    }
}