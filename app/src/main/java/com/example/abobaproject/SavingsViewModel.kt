package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.model.*
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch
import java.util.UUID

class SavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)

    private val _savingsAccounts = mutableStateListOf<SavingsAccountResponse>()
    val savingsAccounts: List<SavingsAccountResponse> get() = _savingsAccounts

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isEmpty by mutableStateOf(false)
        private set

    init {
        _savingsAccounts.addAll(
            listOf(
                SavingsAccountResponse(
                    id = UUID.randomUUID().toString(),
                    name = "Отпуск на море 🏖️",
                    targetAmount = 150000.0,
                    currentBalance = 65000.0,
                    currency = "RUB",
                    progress = 0.43,
                    createdAt = "2026-01-15"
                ),
                SavingsAccountResponse(
                    id = UUID.randomUUID().toString(),
                    name = "Новый ноутбук 💻",
                    targetAmount = 120000.0,
                    currentBalance = 90000.0,
                    currency = "RUB",
                    progress = 0.75,
                    createdAt = "2026-02-01"
                )
            )
        )
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val accounts = api.getSavingsAccounts()
                if (accounts.isNotEmpty()) {
                    _savingsAccounts.clear()
                    _savingsAccounts.addAll(accounts)
                }
                isEmpty = _savingsAccounts.isEmpty()
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка загрузки с бэкенда (HTTP 500 / сети), используем локальный режим", e)
                isEmpty = _savingsAccounts.isEmpty()
            } finally {
                isLoading = false
            }
        }
    }

    fun createAccount(name: String, targetAmount: Double, currency: String, onSuccess: () -> Unit) {
        val tempAccount = SavingsAccountResponse(
            id = UUID.randomUUID().toString(),
            name = name,
            targetAmount = targetAmount,
            currentBalance = 0.0,
            currency = currency,
            progress = 0.0,
            createdAt = java.time.LocalDate.now().toString()
        )
        _savingsAccounts.add(tempAccount)
        isEmpty = false
        onSuccess()

        viewModelScope.launch {
            try {
                val request = CreateSavingsAccountRequest(name, targetAmount, currency)
                val created = api.createSavingsAccount(request)
                val index = _savingsAccounts.indexOfFirst { it.id == tempAccount.id }
                if (index != -1) _savingsAccounts[index] = created
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка создания на сервере", e)
            }
        }
    }

    fun updateAccount(id: String, name: String, targetAmount: Double, onSuccess: () -> Unit) {
        val index = _savingsAccounts.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _savingsAccounts[index]
            val newProgress = if (targetAmount > 0) (old.currentBalance / targetAmount).coerceAtMost(1.0) else 0.0
            _savingsAccounts[index] = old.copy(name = name, targetAmount = targetAmount, progress = newProgress)
        }
        onSuccess()

        viewModelScope.launch {
            try {
                val request = UpdateSavingsAccountRequest(name, targetAmount)
                api.updateSavingsAccount(id, request)
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка обновления на сервере", e)
            }
        }
    }

    fun depositToAccount(id: String, amount: Double, onSuccess: () -> Unit) {
        val index = _savingsAccounts.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _savingsAccounts[index]
            val newBalance = old.currentBalance + amount
            val newProgress = if (old.targetAmount > 0) (newBalance / old.targetAmount).coerceAtMost(1.0) else 0.0
            _savingsAccounts[index] = old.copy(currentBalance = newBalance, progress = newProgress)
        }
        onSuccess()

        viewModelScope.launch {
            try {
                api.depositToSavings(id, DepositRequest(amount))
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка пополнения на сервере", e)
            }
        }
    }

    fun deleteAccount(id: String) {
        _savingsAccounts.removeAll { it.id == id }
        isEmpty = _savingsAccounts.isEmpty()

        viewModelScope.launch {
            try {
                api.deleteSavingsAccount(id)
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка удаления на сервере", e)
            }
        }
    }

    fun clearError() { errorMessage = null }
}
