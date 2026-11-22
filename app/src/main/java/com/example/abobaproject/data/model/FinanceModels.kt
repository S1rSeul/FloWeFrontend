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

// --- Модели для Расходов (M-01 & M-05) ---

data class ExpenseResponse(
    val id: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val payer: String, // "ME", "PARTNER", "BOTH"
    val date: String,
    val description: String? = null,
    val createdById: String? = null
)

data class CreateExpenseRequest(
    val amount: Double,
    val currency: String,
    val category: String,
    val payer: String,
    val date: String,
    val description: String? = null
)

data class ExpenseListResponse(
    val items: List<ExpenseResponse>,
    val nextCursor: String? = null
)

// --- Модели для Бюджетов (M-04) ---

data class BudgetResponse(
    val id: String,
    val category: String?, // null если общий лимит
    val limitAmount: Double,
    val currentAmount: Double,
    val currency: String,
    val period: String? = "MONTH"
)

data class SetBudgetRequest(
    val category: String?,
    val limitAmount: Double,
    val currency: String = "RUB"
)
