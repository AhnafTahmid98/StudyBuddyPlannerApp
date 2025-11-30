package com.example.studybuddyplannerapp.data

import java.time.LocalDate

/**
 * Single study task in the planner.
 *
 * @param id      Stable unique id (e.g. System.currentTimeMillis()).
 * @param title   Short description of the task.
 * @param subject Course / topic.
 * @param date    Due date.
 * @param isDone  Whether the task is already completed.
 */
data class Task(
    val id: Long = 0L,
    val title: String,
    val subject: String,
    val date: LocalDate,
    val isDone: Boolean = false
)
