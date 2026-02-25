package com.example.studentcompanion.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val courseCode: String,
    val courseName: String,
    val instructor: String,
    val room: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val color: String
)