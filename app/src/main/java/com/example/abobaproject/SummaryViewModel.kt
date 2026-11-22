package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.model.ExpenseSummaryResponse
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)

    var summary by mutableStateOf<ExpenseSummaryResponse?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadSummary() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Заглушка: берем расходы за последние 30 дней
                val to = Instant.now().toString()
                val from = Instant.now().minus(30, ChronoUnit.DAYS).toString()

                summary = api.getExpenseSummary(from = from, to = to)
            } catch (e: Exception) {
                Log.e("SummaryViewModel", "Ошибка загрузки сводки", e)
                errorMessage = "Не удалось загрузить сводку"
            } finally {
                isLoading = false
            }
        }
    }
}