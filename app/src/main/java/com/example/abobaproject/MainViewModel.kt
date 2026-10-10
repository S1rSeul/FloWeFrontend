package com.example.abobaproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    // Код приглашения (в реальном приложении приходит с сервера)
    var inviteCode by mutableStateOf<String?>(null)
        private set

    // Заглушка: проверяем, привязан ли пользователь к пространству пары
    var hasSpace by mutableStateOf(false)
        private set

    fun createNewSpace(): String {
        // Имитация создания пространства на бэкенде FloWe
        val code = (100000..999999).random().toString()
        inviteCode = code
        return code
    }

    fun joinSpaceByCode(code: String): Boolean {
        if (code.length == 6) {
            hasSpace = true
            return true
        }
        return false
    }

    fun onPartnerJoined() {
        hasSpace = true
    }

    fun logout() {
        hasSpace = false
        inviteCode = null
    }
}