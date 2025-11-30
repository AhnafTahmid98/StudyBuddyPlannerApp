package com.example.studybuddyplannerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studybuddyplannerapp.data.Task
import com.example.studybuddyplannerapp.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for StudyBuddy tasks.
 *
 * - Holds the current task list in memory.
 * - Persists every change via [TaskRepository] (DataStore).
 * - Provides simple stats for the Stats screen.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // In-memory list, backed by DataStore via TaskRepository.
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        // Observe tasks from repository. Keep a small sample list on first run.
        viewModelScope.launch {
            repository.tasksFlow.collect { storedTasks ->
                if (storedTasks.isEmpty()) {
                    val sample = defaultSampleTasks()
                    _tasks.value = sample
                    // Persist the initial sample once so it survives app restarts.
                    repository.saveTasks(sample)
                } else {
                    _tasks.value = storedTasks
                }
            }
        }
    }

    /** Default sample content so the Home screen is not empty on first launch. */
    private fun defaultSampleTasks(): List<Task> {
        return listOf(
            Task(
                id = 1L,
                title = "Math revision",
                subject = "Math",
                date = LocalDate.now()
            ),
            Task(
                id = 2L,
                title = "Read chapter 3",
                subject = "Physics",
                date = LocalDate.now().plusDays(1)
            )
        )
    }

    /** Add a new task and persist the updated list. */
    fun addTask(title: String, subject: String, date: LocalDate) {
        if (title.isBlank() || subject.isBlank()) return

        val cleanedTitle = title.trim()
        val cleanedSubject = subject.trim()

        val newTask = Task(
            id = System.currentTimeMillis(),   // quick unique id
            title = cleanedTitle,
            subject = cleanedSubject,
            date = date
        )

        val updated = _tasks.value + newTask
        updateTasks(updated)
    }

    /** Toggle the "done" state of the task with the given [id]. */
    fun toggleDone(id: Long) {
        val updated = _tasks.value.map { task ->
            if (task.id == id) task.copy(isDone = !task.isDone) else task
        }
        updateTasks(updated)
    }

    /** Clear all tasks (used by the Stats screen) and persist the change. */
    fun clearAll() {
        updateTasks(emptyList())
    }

    /**
     * Internal helper that updates the in-memory list and writes it to DataStore.
     */
    private fun updateTasks(newList: List<Task>) {
        _tasks.value = newList
        viewModelScope.launch {
            repository.saveTasks(newList)
        }
    }
}

/**
 * Pure function so it is easy to unit test.
 *
 * @return Triple(total, done, pending)
 */
fun calculateTaskStats(tasks: List<Task>): Triple<Int, Int, Int> {
    val all = tasks.size
    val done = tasks.count { it.isDone }
    val pending = all - done
    return Triple(all, done, pending)
}

/**
 * Factory so MainActivity can create [TaskViewModel] with a [TaskRepository].
 */
class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
