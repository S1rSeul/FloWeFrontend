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

    // УДАЛЕН блок init с мок-данными (Отпуск, Ноутбук)

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val accounts = api.getSavingsAccounts()
                _savingsAccounts.clear() // Всегда очищаем перед загрузкой
                _savingsAccounts.addAll(accounts)
                isEmpty = _savingsAccounts.isEmpty()
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка загрузки", e)
                errorMessage = "Ошибка сети"
                isEmpty = _savingsAccounts.isEmpty()
            } finally {
                isLoading = false
            }
        }
    }

    fun createAccount(name: String, targetAmount: Double, currency: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Убрали оптимистичное добавление, ждем ответа сервера
                val request = CreateSavingsAccountRequest(name, targetAmount, currency)
                val created = api.createSavingsAccount(request)
                _savingsAccounts.add(created)
                isEmpty = false
                onSuccess()
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка создания", e)
                errorMessage = "Не удалось создать счет"
            }
        }
    }

    fun updateAccount(id: String, name: String, targetAmount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = UpdateSavingsAccountRequest(name, targetAmount)
                // Берем обновленный объект с сервера и заменяем старый
                val updated = api.updateSavingsAccount(id, request)
                val index = _savingsAccounts.indexOfFirst { it.id == id }
                if (index != -1) {
                    _savingsAccounts[index] = updated
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка обновления", e)
                errorMessage = "Не удалось обновить счет"
            }
        }
    }

    fun depositToAccount(id: String, amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Отправляем запрос и берем результат с сервера (там обновленный баланс и прогресс)
                val updated = api.depositToSavings(id, DepositRequest(amount))
                val index = _savingsAccounts.indexOfFirst { it.id == id }
                if (index != -1) {
                    _savingsAccounts[index] = updated
                }
                onSuccess()
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Ошибка пополнения", e)
                errorMessage = "Не удалось пополнить счет"
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
                Log.e("SavingsViewModel", "Ошибка удаления", e)
            }
        }
    }

    fun clearError() { errorMessage = null }
}