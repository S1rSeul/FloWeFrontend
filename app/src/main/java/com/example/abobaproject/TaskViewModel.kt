package com.example.abobaproject

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abobaproject.data.TaskMapper
import com.example.abobaproject.data.model.*
import com.example.abobaproject.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val api = NetworkModule.provideApiService(application)

    // === Данные ===
    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks

    var members by mutableStateOf<List<MemberResponse>>(emptyList())
        private set

    var isPartnerOnline by mutableStateOf(false)
        private set

    // === AND-34: визуальные состояния ===
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isEmpty by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    /** Загрузка задач + участников пары */
    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val membersList = api.getMembers()
                members = membersList

                val tasksPage = api.getTasks(sort = "createdAt", order = "desc")
                _tasks.clear()
                _tasks.addAll(tasksPage.items.map { TaskMapper.toClientTask(it, membersList) })

                isEmpty = _tasks.isEmpty()
                Log.d("TaskViewModel", "Загружено задач: ${_tasks.size}, участников: ${membersList.size}")
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                Log.e("TaskViewModel", "HTTP ошибка: $body", e)
                errorMessage = "Ошибка сервера: ${e.code()}"
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка сети: ${e.message}", e)
                errorMessage = e.message ?: "Ошибка сети"
            } finally {
                isLoading = false
            }
        }
    }

    // AND-25 + AND-33: Оптимистичное переключение статуса
    fun toggleTaskCompleted(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index == -1) return
        val old = _tasks[index]
        val newStatus = if (old.isCompleted) "todo" else "done"

        // Оптимистично обновляем UI сразу
        _tasks[index] = old.copy(isCompleted = !old.isCompleted)

        viewModelScope.launch {
            try {
                val updated = api.updateTaskStatus(taskId, UpdateTaskStatusRequest(newStatus))
                // Синхронизируем с ответом сервера (на случай, если он что-то изменил)
                val fresh = TaskMapper.toClientTask(updated, members)
                val i = _tasks.indexOfFirst { it.id == taskId }
                if (i != -1) _tasks[i] = fresh
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка смены статуса — откат: ${e.message}", e)
                // Откат оптимистичного изменения
                val i = _tasks.indexOfFirst { it.id == taskId }
                if (i != -1) _tasks[i] = old
                errorMessage = "Не удалось изменить статус"
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        deadlineEpochMillis: Long?,
        assignee: TaskAssignee
    ) {
        if (title.isBlank()) return

        // Оптимистично создаём временную задачу
        val tempId = "temp_${System.currentTimeMillis()}"
        val optimistic = Task(
            id = tempId,
            title = title.trim(),
            description = description.trim(),
            deadlineEpochMillis = deadlineEpochMillis,
            assignee = assignee
        )
        _tasks.add(0, optimistic)
        isEmpty = false

        viewModelScope.launch {
            try {
                val request = CreateTaskRequest(
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    assigneeId = TaskMapper.assigneeToId(assignee, members),
                    deadline = TaskMapper.millisToIso(deadlineEpochMillis)
                )
                val created = api.createTask(request)
                // Заменяем временную задачу реальной
                val idx = _tasks.indexOfFirst { it.id == tempId }
                if (idx != -1) {
                    _tasks[idx] = TaskMapper.toClientTask(created, members)
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка создания: ${e.message}", e)
                _tasks.removeAll { it.id == tempId }
                errorMessage = "Не удалось создать задачу"
            }
        }
    }

    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        deadlineEpochMillis: Long?,
        assignee: TaskAssignee,
        isCompleted: Boolean
    ) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index == -1) return
        val old = _tasks[index]

        // Оптимистичное обновление
        _tasks[index] = old.copy(
            title = title.trim(),
            description = description.trim(),
            deadlineEpochMillis = deadlineEpochMillis,
            assignee = assignee,
            isCompleted = isCompleted
        )

        viewModelScope.launch {
            try {
                val request = UpdateTaskRequest(
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    assigneeId = TaskMapper.assigneeToId(assignee, members),
                    deadline = TaskMapper.millisToIso(deadlineEpochMillis)
                )
                val updated = api.updateTask(taskId, request)
                val i = _tasks.indexOfFirst { it.id == taskId }
                if (i != -1) _tasks[i] = TaskMapper.toClientTask(updated, members)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка обновления — откат", e)
                val i = _tasks.indexOfFirst { it.id == taskId }
                if (i != -1) _tasks[i] = old
                errorMessage = "Не удалось сохранить изменения"
            }
        }
    }

    fun deleteTask(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index == -1) return
        val removed = _tasks.removeAt(index)

        viewModelScope.launch {
            try {
                api.deleteTask(taskId)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Ошибка удаления — откат", e)
                _tasks.add(index, removed)
                errorMessage = "Не удалось удалить задачу"
            }
        }
    }

    fun getTaskById(taskId: String): Task? = _tasks.find { it.id == taskId }

    fun togglePartnerStatus() {
        isPartnerOnline = !isPartnerOnline
    }

    fun clearError() {
        errorMessage = null
    }
}