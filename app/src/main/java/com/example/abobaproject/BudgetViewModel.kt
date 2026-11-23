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

    // Блок init с фейковыми данными УДАЛЕН

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

    fun setBudget(
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
        // Переводим русское название категории ("Еда") в код сервера ("FOOD")
        val serverCategory = if (category == "Общий") null else FinanceMapper.categoryToServer(category)

        viewModelScope.launch {
            try {
                // Создаем запрос по новой модели
                val request = CreateBudgetRequest(
                    category = serverCategory,
                    limitAmount = limit,
                    currency = currency
                )

                // Отправляем на сервер и получаем ответ
                val createdBudget = api.createBudget(request)

                // Добавляем реальный ответ от сервера в список (сервер сам посчитал spentAmount и т.д.)
                // Проверяем, нет ли уже бюджета на эту категорию, чтобы обновить, а не дублировать
                val index = _budgets.indexOfFirst { it.category == createdBudget.category }
                if (index != -1) {
                    _budgets[index] = createdBudget
                } else {
                    _budgets.add(createdBudget)
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Ошибка отправки лимита на бэкенд", e)
                errorMessage = "Не удалось сохранить бюджет"
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