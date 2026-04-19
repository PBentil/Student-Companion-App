package com.example.studentcompanion.model

import com.example.studentcompanion.database.TaskEntity

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            isCompleted = isCompleted,
            createdAt = createdAt
        )
    }
}

// Extension function for converting entity to model
fun TaskEntity.toTask(): Task {
    return Task(
        id = this.id,
        title = this.title,
        description = this.description,
        category = this.category,
        isCompleted = this.isCompleted,
        createdAt = this.createdAt
    )
}