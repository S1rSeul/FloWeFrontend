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
import com.example.abobaproject.data.model.BudgetResponse
import com.example.abobaproject.data.model.CreateBudgetRequest
import com.example.abobaproject.data.model.UpdateBudgetRequest
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)

    private val _budgets = mutableStateListOf<BudgetResponse>()
    val budgets: List<BudgetResponse> get() = _budgets

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadBudgets() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val remoteBudgets = api.getBudgets()
                _budgets.clear()
                _budgets.addAll(remoteBudgets)
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Ошибка загрузки бюджетов", e)
                errorMessage = "Не удалось загрузить бюджеты"
            } finally {
                isLoading = false
            }
        }
    }

    // budgetId != null — редактирование, budgetId == null — создание
    fun saveBudget(
        budgetId: String?,
        category: String,
        limitAmountStr: String,
        currency: String = "RUB",
        onSuccess: () -> Unit
    ) {
        val limit = limitAmountStr.toDoubleOrNull()
        if (limit == null || limit <= 0.0) {
            errorMessage = "Введите корректный лимит больше нуля"
            return
        }
        errorMessage = null

        viewModelScope.launch {
            try {
                if (budgetId != null) {
                    // Редактирование — PATCH /budgets/{id}
                    val request = UpdateBudgetRequest(limitAmount = limit)
                    val updated = api.updateBudget(budgetId, request)
                    val index = _budgets.indexOfFirst { it.id == budgetId }
                    if (index != -1) {
                        _budgets[index] = updated
                    }
                } else {
                    // Создание — POST /budgets
                    // Отправляем "OTHER" вместо null для "Общего" бюджета, чтобы сервер не ругался
                    val serverCategory = FinanceMapper.categoryToServer(category)

                    val request = CreateBudgetRequest(
                        category = serverCategory,
                        limitAmount = limit,
                        currency = currency
                    )
                    val created = api.createBudget(request)
                    _budgets.add(created)
                }
                onSuccess()
            } catch (e: retrofit2.HttpException) {
                // Если сервер вернул ошибку (400, 409, 500), мы вытащим её текст
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("BudgetViewModel", "ОШИБКА СЕРВЕРА: Код ${e.code()}, Ответ: $errorBody", e)
                errorMessage = "Ошибка сервера: ${e.code()}"
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "ОШИБКА СЕТИ", e)
                errorMessage = "Ошибка сети"
            }
        }
    }

    fun deleteBudget(id: String) {
        _budgets.removeAll { it.id == id }
        viewModelScope.launch {
            try {
                api.deleteBudget(id)
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Ошибка удаления лимита на бэкенде", e)
            }
        }
    }
}