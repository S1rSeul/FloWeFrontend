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
    val name: String?, // В Swagger это JsonNode, на клиенте лучше использовать nullable поля
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