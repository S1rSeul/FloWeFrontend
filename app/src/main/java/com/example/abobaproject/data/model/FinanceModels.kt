package com.example.abobaproject.data.model

// --- Модели для Накопительных счетов (M-03) ---

data class SavingsAccountResponse(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentBalance: Double,
    val currency: String,
    val progress: Double,
    val createdAt: String
)

data class CreateSavingsAccountRequest(
    val name: String,
    val targetAmount: Double,
    val currency: String
)

data class UpdateSavingsAccountRequest(
    val name: String?,
    val targetAmount: Double?
)

data class DepositRequest(
    val amount: Double
)

// --- Модели для Сводки расходов (M-02) ---

data class ExpenseSummaryResponse(
    val from: String,
    val to: String,
    val summaries: List<CurrencySummary>
)

data class CurrencySummary(
    val currency: String,
    val totalAmount: Double,
    val myPaidAmount: Double,
    val partnerPaidAmount: Double
)

data class CreateExpenseRequest(
    val amount: Double,
    val myPaidAmount: Double,      // Сколько заплатил я
    val partnerPaidAmount: Double, // Сколько заплатил партнер
    val currency: String,
    val category: String,          // Только английские коды: FOOD, TRANSPORT...
    val spentAt: String,           // Дата в формате ISO: "2026-11-23T12:00:00Z"
    val description: String? = null
)

// Модель для РЕДАКТИРОВАНИЯ (PATCH запрос)
data class UpdateExpenseRequest(
    val amount: Double? = null,
    val myPaidAmount: Double? = null,
    val partnerPaidAmount: Double? = null,
    val currency: String? = null,
    val category: String? = null,
    val spentAt: String? = null,
    val description: String? = null
)

// Модель, которую ПОЛУЧАЕМ с сервера
data class ExpenseResponse(
    val id: String,
    val amount: Double,
    val myPaidAmount: Double,
    val partnerPaidAmount: Double,
    val currency: String,
    val category: String,
    val spentAt: String, // Дата в ISO
    val description: String? = null,
    val createdAt: String? = null
) {
    // Добавляем локальное свойство для UI, чтобы твой код товарища (который использует payer) не сломался
    val payer: String
        get() = when {
            myPaidAmount == amount && partnerPaidAmount == 0.0 -> "ME"
            partnerPaidAmount == amount && myPaidAmount == 0.0 -> "PARTNER"
            else -> "BOTH"
        }

    // Локальное свойство для красивой даты в UI (первые 10 символов ISO)
    val date: String get() = spentAt.take(10)
}

// История расходов (возвращается с GET /expenses)
data class ExpenseHistoryResponse(
    val items: List<ExpenseResponse>,
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

// --- БЮДЖЕТЫ (ИСПРАВЛЕНО ПОД НОВЫЙ SWAGGER) ---
data class CreateBudgetRequest(
    val category: String? = null, // null = общий бюджет
    val limitAmount: Double,
    val currency: String = "RUB"
)

data class BudgetResponse(
    val id: String,
    val category: String?,
    val limitAmount: Double,
    val currency: String,
    val period: String? = "MONTHLY",
    val spentAmount: Double = 0.0,   // Сервер САМ считает, сколько потрачено!
    val remainingAmount: Double = 0.0,
    val usagePercent: Double = 0.0,
    val isExceeded: Boolean = false  // И САМ говорит, превышен ли лимит!
)

data class UpdateBudgetRequest(
    val limitAmount: Double
)