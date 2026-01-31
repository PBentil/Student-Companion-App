package com.example.studentcompanion.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseCode: String,
    val courseName: String,
    val instructor: String,
    val room: String,
    val schedule: String,
    val credits: Int,
    val color: String
)