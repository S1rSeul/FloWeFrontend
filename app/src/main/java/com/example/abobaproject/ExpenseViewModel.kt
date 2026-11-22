package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.model.CreateExpenseRequest
import com.example.abobaproject.data.model.ExpenseResponse
import com.example.abobaproject.data.remote.NetworkModule
import com.example.abobaproject.ui.components.PayerType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)

    private val _expenses = mutableStateListOf<ExpenseResponse>()
    val expenses: List<ExpenseResponse> get() = _expenses

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedCategoryFilter by mutableStateOf("Все")
    var selectedPayerFilter by mutableStateOf("Все")

    init {
        _expenses.addAll(
            listOf(
                ExpenseResponse(
                    id = UUID.randomUUID().toString(),
                    amount = 3500.0,
                    currency = "RUB",
                    category = "Еда",
                    payer = "ME",
                    date = LocalDate.now().toString(),
                    description = "Покупка продуктов в гипермаркете"
                ),
                ExpenseResponse(
                    id = UUID.randomUUID().toString(),
                    amount = 1200.0,
                    currency = "RUB",
                    category = "Транспорт",
                    payer = "PARTNER",
                    date = LocalDate.now().minusDays(1).toString(),
                    description = "Заправка авто"
                ),
                ExpenseResponse(
                    id = UUID.randomUUID().toString(),
                    amount = 4800.0,
                    currency = "RUB",
                    category = "Развлечения",
                    payer = "BOTH",
                    date = LocalDate.now().minusDays(2).toString(),
                    description = "Билеты в театр"
                )
            )
        )
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val remoteExpenses = api.getExpenses()
                if (remoteExpenses.isNotEmpty()) {
                    _expenses.clear()
                    _expenses.addAll(remoteExpenses)
                }
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Ошибка загрузки расходов из сети, работаем в локальном/оптимистичном режиме", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun addExpense(
        amountStr: String,
        currency: String,
        category: String,
        payer: PayerType,
        dateStr: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            errorMessage = "Введите корректную сумму больше нуля"
            return
        }
        if (category.isBlank()) {
            errorMessage = "Выберите категорию расхода"
            return
        }

        errorMessage = null

        val payerCode = when (payer) {
            PayerType.ME -> "ME"
            PayerType.PARTNER -> "PARTNER"
            PayerType.BOTH -> "BOTH"
        }

        val tempExpense = ExpenseResponse(
            id = UUID.randomUUID().toString(),
            amount = amount,
            currency = currency,
            category = category,
            payer = payerCode,
            date = dateStr,
            description = description.takeIf { it.isNotBlank() }
        )

        _expenses.add(0, tempExpense)
        onSuccess()

        viewModelScope.launch {
            try {
                val request = CreateExpenseRequest(
                    amount = amount,
                    currency = currency,
                    category = category,
                    payer = payerCode,
                    date = dateStr,
                    description = description.takeIf { it.isNotBlank() }
                )
                val created = api.createExpense(request)
                val index = _expenses.indexOfFirst { it.id == tempExpense.id }
                if (index != -1) {
                    _expenses[index] = created
                }
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Ошибка сохранения расхода на бэкенде", e)
            }
        }
    }

    fun updateExpense(
        id: String,
        amountStr: String,
        currency: String,
        category: String,
        payer: PayerType,
        dateStr: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            errorMessage = "Введите корректную сумму больше нуля"
            return
        }
        if (category.isBlank()) {
            errorMessage = "Выберите категорию расхода"
            return
        }

        errorMessage = null

        val payerCode = when (payer) {
            PayerType.ME -> "ME"
            PayerType.PARTNER -> "PARTNER"
            PayerType.BOTH -> "BOTH"
        }

        val index = _expenses.indexOfFirst { it.id == id }
        if (index != -1) {
            _expenses[index] = _expenses[index].copy(
                amount = amount,
                currency = currency,
                category = category,
                payer = payerCode,
                date = dateStr,
                description = description.takeIf { it.isNotBlank() }
            )
        }
        onSuccess()
    }

    fun deleteExpense(id: String) {
        _expenses.removeAll { it.id == id }
        viewModelScope.launch {
            try {
                api.deleteExpense(id)
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Ошибка удаления расхода на сервере", e)
            }
        }
    }

    val filteredExpenses: List<ExpenseResponse>
        get() {
            return _expenses.filter { expense ->
                val categoryMatch = selectedCategoryFilter == "Все" || expense.category.equals(selectedCategoryFilter, ignoreCase = true)
                val payerMatch = when (selectedPayerFilter) {
                    "Я" -> expense.payer == "ME"
                    "Партнёр" -> expense.payer == "PARTNER"
                    "Пополам" -> expense.payer == "BOTH"
                    else -> true
                }
                categoryMatch && payerMatch
            }
        }

    val totalFilteredAmount: Double
        get() = filteredExpenses.sumOf { it.amount }
}
