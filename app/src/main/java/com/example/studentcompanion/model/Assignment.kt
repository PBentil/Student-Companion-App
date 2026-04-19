package com.example.studentcompanion.model

import com.example.studentcompanion.database.AssignmentEntity

data class Assignment(
    val id: Long = 0,
    val title: String,
    val courseCode: String,
    val description: String = "",
    val dueDate: String,
    val priority: Priority = Priority.MEDIUM,
    val status: Status = Status.PENDING,
    val isCompleted: Boolean = false
) {
    fun toEntity(): AssignmentEntity {
        return AssignmentEntity(
            id = id,
            title = title,
            courseCode = courseCode,
            description = description,
            dueDate = dueDate,
            priority = priority.name,
            status = status.name,
            isCompleted = isCompleted
        )
    }
}

fun AssignmentEntity.toAssignment(): Assignment {
    return Assignment(
        id = this.id,
        title = this.title,
        courseCode = this.courseCode,
        description = this.description,
        dueDate = this.dueDate,
        priority = Priority.valueOf(this.priority),
        status = Status.valueOf(this.status),
        isCompleted = this.isCompleted
    )
}

enum class Priority {
    LOW, MEDIUM, HIGH
}

enum class Status {
    PENDING, COMPLETED, OVERDUE
}