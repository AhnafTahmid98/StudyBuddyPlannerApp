package com.example.studybuddyplannerapp

import com.example.studybuddyplannerapp.data.Task
import com.example.studybuddyplannerapp.ui.viewmodel.calculateTaskStats
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TaskStatsTest {

    @Test
    fun calculateStats_countsCorrectly() {
        val today = LocalDate.now()

        val tasks = listOf(
            Task(
                id = 1,
                title = "Task 1",
                subject = "Math",
                date = today,
                isDone = false
            ),
            Task(
                id = 2,
                title = "Task 2",
                subject = "Physics",
                date = today,
                isDone = true
            ),
            Task(
                id = 3,
                title = "Task 3",
                subject = "English",
                date = today,
                isDone = true
            )
        )

        val (total, done, pending) = calculateTaskStats(tasks)

        assertEquals(3, total)
        assertEquals(2, done)
        assertEquals(1, pending)
    }
}
