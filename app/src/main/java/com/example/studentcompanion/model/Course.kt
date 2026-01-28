package com.example.studentcompanion.model

data class Course(
    val id: Long,
    val courseCode: String,
    val courseName: String,
    val instructor: String,
    val room: String,
    val schedule: String,
    val credits: Int,
    val color: String
)
