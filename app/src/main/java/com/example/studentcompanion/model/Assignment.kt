package com.example.studentcompanion.model

import com.example.studentcompanion.model.Priority
import com.example.studentcompanion.model.Status

data class Assignment(
    val id: Long,
    val title: String,
    val courseCode: String,
    val description: String,
    val dueDate: String,
    val priority: Priority,
    val status: Status,
    val isCompleted: Boolean
)
