package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.local.TokenManager
import com.example.abobaproject.data.model.CoupleResponse
import com.example.abobaproject.data.model.CreateCoupleRequest
import com.example.abobaproject.data.model.JoinCoupleRequest
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class SpaceState {
    object Loading : SpaceState()
    object NoSpace : SpaceState()
    data class WaitingPartner(val couple: CoupleResponse) : SpaceState()
    data class Active(val couple: CoupleResponse) : SpaceState()
    data class Error(val message: String) : SpaceState()
}

class SpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)
    private val tokenManager = TokenManager(application)   // 👈 добавили

    var spaceState by mutableStateOf<SpaceState>(SpaceState.Loading)
        private set

    var actionError by mutableStateOf<String?>(null)
        private set

    var isActionLoading by mutableStateOf(false)
        private set

    fun checkSpaceStatus() {
        viewModelScope.launch {
            spaceState = SpaceState.Loading
            try {
                val couple = api.getCoupleStatus()
                spaceState = when (couple.status.lowercase()) {
                    "active" -> SpaceState.Active(couple)
                    else -> SpaceState.WaitingPartner(couple)
                }
            } catch (e: HttpException) {
                if (e.code() == 404) spaceState = SpaceState.NoSpace
                else spaceState = SpaceState.Error("Ошибка сервера: ${e.code()}")
            } catch (e: Exception) {
                spaceState = SpaceState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    // AND-12: Создать новое пространство
    fun createCouple(name: String = "Наше пространство") {
        viewModelScope.launch {
            isActionLoading = true
            actionError = null
            try {
                Log.d("SpaceViewModel", "Создаём пару...")
                val couple = api.createCouple(CreateCoupleRequest(name))
                Log.d("SpaceViewModel", "Пара создана: ${couple.inviteCode}")
                spaceState = SpaceState.WaitingPartner(couple)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SpaceViewModel", "Ошибка создания: $errorBody", e)
                actionError = errorBody ?: "Ошибка ${e.code()}"
            } catch (e: Exception) {
                Log.e("SpaceViewModel", "Ошибка сети: ${e.message}", e)
                actionError = e.message ?: "Ошибка сети"
            } finally {
                isActionLoading = false
            }
        }
    }

    // AND-15: Вступить в существующее пространство
    fun joinCouple(inviteCode: String) {
        viewModelScope.launch {
            isActionLoading = true
            actionError = null
            try {
                Log.d("SpaceViewModel", "Вступаем в пару по коду $inviteCode...")
                val couple = api.joinCouple(JoinCoupleRequest(inviteCode.trim()))
                Log.d("SpaceViewModel", "Вступили! Статус: ${couple.status}")
                spaceState = when (couple.status.lowercase()) {
                    "active" -> SpaceState.Active(couple)
                    else -> SpaceState.WaitingPartner(couple)
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SpaceViewModel", "Ошибка вступления: $errorBody", e)
                actionError = errorBody ?: "Ошибка ${e.code()}"
            } catch (e: Exception) {
                Log.e("SpaceViewModel", "Ошибка сети: ${e.message}", e)
                actionError = e.message ?: "Ошибка сети"
            } finally {
                isActionLoading = false
            }
        }
    }

    // 🆕 AND-10 + смена аккаунта: полный logout
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("SpaceViewModel", "Logout: начинаем очистку...")
            isActionLoading = true
            try {
                // TODO: AND-10 — вызвать api.logout(refreshToken), когда решим вопрос с телом
            } finally {
                // Гарантированно дожидаемся очистки DataStore
                tokenManager.clearTokens()
                Log.d("SpaceViewModel", "Токены очищены")

                // Сбрасываем состояние, чтобы при следующем входе не было закэшированных данных
                spaceState = SpaceState.Loading
                actionError = null
                isActionLoading = false

                onComplete()
            }
        }
    }

    fun reset() {
        spaceState = SpaceState.Loading
        actionError = null
    }
}