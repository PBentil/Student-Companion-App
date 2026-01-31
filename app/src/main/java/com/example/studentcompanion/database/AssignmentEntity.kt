package com.example.studentcompanion.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val courseCode: String,
    val description: String,
    val dueDate: String,
    val priority: String, // "LOW", "MEDIUM", "HIGH"
    val status: String, // "PENDING", "COMPLETED", "OVERDUE"
    val isCompleted: Boolean
)