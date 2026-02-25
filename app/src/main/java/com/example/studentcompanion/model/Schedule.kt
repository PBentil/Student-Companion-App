package com.example.studentcompanion.model

import com.example.studentcompanion.database.ScheduleEntity

data class Schedule(
    val id: Long = 0,
    val courseId: Long,
    val courseCode: String,
    val courseName: String,
    val instructor: String,
    val room: String,
    val dayOfWeek: String, // Monday, Tuesday, etc.
    val startTime: String, // HH:mm format (e.g., "09:00")
    val endTime: String,   // HH:mm format (e.g., "10:30")
    val color: String = "#6366F1"
) {
    fun toEntity(): ScheduleEntity {
        return ScheduleEntity(
            id = id,
            courseId = courseId,
            courseCode = courseCode,
            courseName = courseName,
            instructor = instructor,
            room = room,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            color = color
        )
    }

    fun getDurationMinutes(): Int {
        val start = startTime.split(":").map { it.toInt() }
        val end = endTime.split(":").map { it.toInt() }

        val startMinutes = start[0] * 60 + start[1]
        val endMinutes = end[0] * 60 + end[1]

        return endMinutes - startMinutes
    }
}

// Extension function for converting entity to model
fun ScheduleEntity.toSchedule(): Schedule {
    return Schedule(
        id = this.id,
        courseId = this.courseId,
        courseCode = this.courseCode,
        courseName = this.courseName,
        instructor = this.instructor,
        room = this.room,
        dayOfWeek = this.dayOfWeek,
        startTime = this.startTime,
        endTime = this.endTime,
        color = this.color
    )
}