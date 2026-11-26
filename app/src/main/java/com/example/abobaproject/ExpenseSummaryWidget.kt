package com.example.abobaproject

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ExpenseSummaryWidget(
    expenseViewModel: ExpenseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // АВТОЗАГРУЗКА расходов при открытии виджета
    LaunchedEffect(Unit) {
        expenseViewModel.loadExpenses()
    }

    val expenses = expenseViewModel.expenses

    val myPaidAmount = remember(expenses.size, expenses.toList()) {
        expenses.filter { it.payer == "ME" }.sumOf { it.amount } +
                expenses.filter { it.payer == "BOTH" }.sumOf { it.amount / 2.0 }
    }

    val partnerPaidAmount = remember(expenses.size, expenses.toList()) {
        expenses.filter { it.payer == "PARTNER" }.sumOf { it.amount } +
                expenses.filter { it.payer == "BOTH" }.sumOf { it.amount / 2.0 }
    }

    val totalAmount = myPaidAmount + partnerPaidAmount
    val myProgress = if (totalAmount > 0) (myPaidAmount / totalAmount).toFloat() else 0.5f

    // Четкие высококонтрастные цвета для легкой различимости секторов
    val myColor = Color(0xFF2563EB)      // Насыщенный яркий синий
    val partnerColor = Color(0xFFEC4899) // Яркий кораллово-розовый

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Вклад в общие расходы (30 дней)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(16.dp))

            if (expenseViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
            } else if (expenses.isEmpty() || totalAmount == 0.0) {
                Text(
                    text = "Нет данных о расходах",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            val sweepMy = myProgress * 360f
                            val sweepPartner = 360f - sweepMy

                            drawArc(
                                color = myColor,
                                startAngle = -90f,
                                sweepAngle = sweepMy,
                                useCenter = false,
                                style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                            )

                            drawArc(
                                color = partnerColor,
                                startAngle = -90f + sweepMy,
                                sweepAngle = sweepPartner,
                                useCenter = false,
                                style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Всего",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${totalAmount.toInt()} ₽",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color = myColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Вы (${(myProgress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = myColor
                                )
                                Text(
                                    text = "${String.format("%.2f", myPaidAmount)} ₽",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color = partnerColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val partnerPercent = 100 - (myProgress * 100).toInt()
                                Text(
                                    text = "Партнёр ($partnerPercent%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = partnerColor
                                )
                                Text(
                                    text = "${String.format("%.2f", partnerPaidAmount)} ₽",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
