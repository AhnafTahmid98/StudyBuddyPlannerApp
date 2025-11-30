package com.example.studybuddyplannerapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// Extension DataStore on Context
private val Context.taskDataStore by preferencesDataStore("tasks_prefs")

class TaskRepository(private val context: Context) {

    companion object {
        private val TASKS_KEY = stringPreferencesKey("tasks_data")
    }

    /**
     * Flow of tasks read from DataStore. If nothing stored yet, returns emptyList().
     */
    val tasksFlow: Flow<List<Task>> = context.taskDataStore.data
        .map { preferences ->
            val raw = preferences[TASKS_KEY] ?: ""
            decodeTasks(raw)
        }

    /**
     * Save the whole task list to DataStore.
     */
    suspend fun saveTasks(tasks: List<Task>) {
        val encoded = encodeTasks(tasks)
        context.taskDataStore.edit { prefs ->
            prefs[TASKS_KEY] = encoded
        }
    }

    /**
     * Simple encoding: each task on its own line:
     * id|title|subject|date|isDone
     */
    private fun encodeTasks(tasks: List<Task>): String {
        return tasks.joinToString(separator = "\n") { task ->
            listOf(
                task.id.toString(),
                task.title.replace("\n", " "),
                task.subject.replace("\n", " "),
                task.date.toString(), // ISO yyyy-MM-dd
                task.isDone.toString()
            ).joinToString(separator = "|")
        }
    }

    private fun decodeTasks(raw: String): List<Task> {
        if (raw.isBlank()) return emptyList()

        return raw.lineSequence().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 5) return@mapNotNull null

            try {
                val id = parts[0].toLong()
                val title = parts[1]
                val subject = parts[2]
                val date = LocalDate.parse(parts[3])
                val done = parts[4].toBoolean()

                Task(
                    id = id,
                    title = title,
                    subject = subject,
                    date = date,
                    isDone = done
                )
            } catch (_: Exception) {
                null
            }
        }.toList()
    }
}
