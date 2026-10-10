package com.example.abobaproject.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// AND-30: Отображение дедлайна с цветовой индикацией
@Composable
fun DeadlineBadge(
    deadlineEpochMillis: Long?,
    modifier: Modifier = Modifier
) {
    if (deadlineEpochMillis == null) return

    val now = System.currentTimeMillis()
    val isOverdue = deadlineEpochMillis < now

    val deadlineCalendar = Calendar.getInstance().apply { timeInMillis = deadlineEpochMillis }
    val nowCalendar = Calendar.getInstance()
    val isToday = deadlineCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
            deadlineCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)

    val (bgColor, textColor, labelPrefix) = when {
        isOverdue -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F),
            "Просрочено: "
        )
        isToday -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Сегодня: "
        )
        else -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Дедлайн: "
        )
    }

    val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale.forLanguageTag("ru"))
    val formattedDate = dateFormat.format(Date(deadlineEpochMillis))

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = "$labelPrefix$formattedDate",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
