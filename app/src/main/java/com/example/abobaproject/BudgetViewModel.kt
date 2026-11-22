package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.model.BudgetResponse
import com.example.abobaproject.data.model.SetBudgetRequest
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch
import java.util.UUID

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)

    private val _budgets = mutableStateListOf<BudgetResponse>()
    val budgets: List<BudgetResponse> get() = _budgets

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        _budgets.addAll(
            listOf(
                BudgetResponse(
                    id = UUID.randomUUID().toString(),
                    category = "Еда",
                    limitAmount = 15000.0,
                    currentAmount = 0.0,
                    currency = "RUB"
                ),
                BudgetResponse(
                    id = UUID.randomUUID().toString(),
                    category = "Транспорт",
                    limitAmount = 10000.0,
                    currentAmount = 0.0,
                    currency = "RUB"
                ),
                BudgetResponse(
                    id = UUID.randomUUID().toString(),
                    category = "Развлечения",
                    limitAmount = 12000.0,
                    currentAmount = 0.0,
                    currency = "RUB"
                )
            )
        )
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val remoteBudgets = api.getBudgets()
                if (remoteBudgets.isNotEmpty()) {
                    _budgets.clear()
                    _budgets.addAll(remoteBudgets)
                }
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Ошибка загрузки бюджетов с сервера, работаем в локальном режиме", e)
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
        val catName = category.takeIf { it.isNotBlank() && it != "Общий" }

        val tempBudget = BudgetResponse(
            id = UUID.randomUUID().toString(),
            category = catName,
            limitAmount = limit,
            currentAmount = 0.0,
            currency = currency
        )

        val index = _budgets.indexOfFirst { (it.category ?: "Общий") == (catName ?: "Общий") }
        if (index != -1) {
            val old = _budgets[index]
            _budgets[index] = old.copy(limitAmount = limit, currency = currency)
        } else {
            _budgets.add(tempBudget)
        }

        onSuccess()

        viewModelScope.launch {
            try {
                val request = SetBudgetRequest(
                    category = catName,
                    limitAmount = limit,
                    currency = currency
                )
                api.setBudget(request)
            } catch (e: Exception) {
                Log.e("BudgetViewModel", "Ошибка отправки лимита на бэкенд", e)
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
