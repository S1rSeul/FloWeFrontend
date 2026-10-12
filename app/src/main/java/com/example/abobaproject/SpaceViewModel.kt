package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.model.CoupleResponse
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

    var spaceState by mutableStateOf<SpaceState>(SpaceState.Loading)
        private set

    fun checkSpaceStatus() {
        viewModelScope.launch {
            spaceState = SpaceState.Loading
            try {
                Log.d("SpaceViewModel", "Запрашиваем статус пары...")
                val couple = api.getCoupleStatus()
                Log.d("SpaceViewModel", "Пара найдена. Статус: ${couple.status}")

                spaceState = when (couple.status.lowercase()) {
                    "active" -> SpaceState.Active(couple)
                    "pending", "waiting" -> SpaceState.WaitingPartner(couple)
                    else -> SpaceState.WaitingPartner(couple)
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.d("SpaceViewModel", "Пары нет → NoSpaceScreen")
                    spaceState = SpaceState.NoSpace
                } else {
                    Log.e("SpaceViewModel", "HTTP ошибка: ${e.code()}", e)
                    spaceState = SpaceState.Error("Ошибка сервера: ${e.code()}")
                }
            } catch (e: Exception) {
                Log.e("SpaceViewModel", "Ошибка сети: ${e.message}", e)
                spaceState = SpaceState.Error(e.message ?: "Ошибка сети")
            }
        }
    }

    fun reset() {
        spaceState = SpaceState.Loading
    }
}