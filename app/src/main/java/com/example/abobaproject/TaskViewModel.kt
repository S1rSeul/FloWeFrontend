package com.example.abobaproject

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {

    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks

    var isPartnerOnline by mutableStateOf(true)
        private set

    init {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        _tasks.addAll(
            listOf(
                Task(
                    title = "Купить продукты к ужину",
                    description = "Молоко, сыр, овощи, хлеб",
                    deadlineEpochMillis = now + oneDayMillis,
                    assignee = TaskAssignee.BOTH,
                    isCompleted = false
                ),
                Task(
                    title = "Записаться к врачу",
                    description = "Плановый осмотр",
                    deadlineEpochMillis = now - oneDayMillis,
                    assignee = TaskAssignee.ME,
                    isCompleted = false
                ),
                Task(
                    title = "Забрать посылку",
                    description = "Пункт выдачи на улице Ленина",
                    deadlineEpochMillis = now + 2 * oneDayMillis,
                    assignee = TaskAssignee.PARTNER,
                    isCompleted = true
                )
            )
        )
    }

    fun addTask(
        title: String,
        description: String,
        deadlineEpochMillis: Long?,
        assignee: TaskAssignee
    ) {
        if (title.isBlank()) return
        val newTask = Task(
            title = title.trim(),
            description = description.trim(),
            deadlineEpochMillis = deadlineEpochMillis,
            assignee = assignee
        )
        _tasks.add(0, newTask)
    }

    fun toggleTaskCompleted(taskId: String) {
        val index = _tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = _tasks[index]
            _tasks[index] = task.copy(isCompleted = !task.isCompleted)
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
        if (index != -1) {
            _tasks[index] = _tasks[index].copy(
                title = title.trim(),
                description = description.trim(),
                deadlineEpochMillis = deadlineEpochMillis,
                assignee = assignee,
                isCompleted = isCompleted
            )
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.removeAll { it.id == taskId }
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.find { it.id == taskId }
    }

    fun togglePartnerStatus() {
        isPartnerOnline = !isPartnerOnline
    }
}
