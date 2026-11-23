package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.FinanceMapper
import com.example.abobaproject.data.model.CreateExpenseRequest
import com.example.abobaproject.data.model.ExpenseResponse
import com.example.abobaproject.data.model.UpdateExpenseRequest
import com.example.abobaproject.data.remote.NetworkModule
import com.example.abobaproject.ui.components.PayerType
import kotlinx.coroutines.launch

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

    var nextCursor by mutableStateOf<String?>(null)
    var hasMore by mutableStateOf(true)

    // УДАЛЕН блок init с фейковыми данными

    fun loadExpenses() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = api.getExpenses()
                _expenses.clear()
                _expenses.addAll(response.items)
                nextCursor = response.nextCursor
                hasMore = response.hasMore
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMoreExpenses() {
        if (isLoading || !hasMore || nextCursor == null) return
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.getExpenses(cursor = nextCursor)
                _expenses.addAll(response.items) // ДОБАВЛЯЕМ в конец, а не очищаем
                nextCursor = response.nextCursor
                hasMore = response.hasMore
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Ошибка подгрузки", e)
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

        // Высчитываем доли
        val myAmount = when (payer) {
            PayerType.ME -> amount
            PayerType.PARTNER -> 0.0
            PayerType.BOTH -> amount / 2.0
        }
        val partnerAmount = amount - myAmount

        viewModelScope.launch {
            try {
                val request = CreateExpenseRequest(
                    amount = amount,
                    myPaidAmount = myAmount,
                    partnerPaidAmount = partnerAmount,
                    currency = currency,
                    category = FinanceMapper.categoryToServer(category), // Перевод "Еда" -> "FOOD"
                    spentAt = FinanceMapper.dateToIso(dateStr),          // Перевод даты в ISO
                    description = description.takeIf { it.isNotBlank() }
                )
                val created = api.createExpense(request)
                _expenses.add(0, created) // Добавляем реальный ответ от сервера
                onSuccess()
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Ошибка сохранения расхода", e)
                errorMessage = "Ошибка сохранения на сервере"
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

        errorMessage = null

        val myAmount = when (payer) {
            PayerType.ME -> amount
            PayerType.PARTNER -> 0.0
            PayerType.BOTH -> amount / 2.0
        }
        val partnerAmount = amount - myAmount

        viewModelScope.launch {
            try {
                val request = UpdateExpenseRequest(
                    amount = amount,
                    myPaidAmount = myAmount,
                    partnerPaidAmount = partnerAmount,
                    currency = currency,
                    category = FinanceMapper.categoryToServer(category),
                    spentAt = FinanceMapper.dateToIso(dateStr),
                    description = description.takeIf { it.isNotBlank() }
                )
                // Отправляем на сервер и обновляем локальный список
                val updated = api.updateExpense(id, request)
                val index = _expenses.indexOfFirst { it.id == id }
                if (index != -1) {
                    _expenses[index] = updated
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Ошибка обновления расхода", e)
                errorMessage = "Ошибка обновления на сервере"
            }
        }
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
                val categoryMatch = selectedCategoryFilter == "Все" ||
                        FinanceMapper.categoryToUi(expense.category).equals(selectedCategoryFilter, ignoreCase = true)
                val payerMatch = when (selectedPayerFilter) {
                    "Я" -> expense.payer == "ME" // expense.payer теперь вычисляется автоматически в модели!
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