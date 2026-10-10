package com.example.abobaproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// AND-19: Индикатор состояния подключения партнёра
@Composable
fun StatusIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val statusColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
    val statusText = if (isOnline) "Партнёр онлайн" else "Партнёр не в сети"

    Surface(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = statusColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
