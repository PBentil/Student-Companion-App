package com.example.studentcompanion.model

import com.example.studentcompanion.database.CourseEntity

data class Course(
    val id: Long = 0,
    val courseCode: String,
    val courseName: String,
    val instructor: String,
    val room: String,
    val schedule: String,
    val credits: Int,
    val color: String = "#6366F1"
) {
    fun toEntity(): CourseEntity {
        return CourseEntity(
            id = id,
            courseCode = courseCode,
            courseName = courseName,
            instructor = instructor,
            room = room,
            schedule = schedule,
            credits = credits,
            color = color
        )
    }
}

fun CourseEntity.toCourse(): Course {
    return Course(
        id = this.id,
        courseCode = this.courseCode,
        courseName = this.courseName,
        instructor = this.instructor,
        room = this.room,
        schedule = this.schedule,
        credits = this.credits,
        color = this.color
    )
}