package com.example.studentcompanion.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedules(): LiveData<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllSchedulesSync(): List<ScheduleEntity>

    @Query("SELECT * FROM schedule WHERE dayOfWeek = :day ORDER BY startTime ASC")
    suspend fun getSchedulesByDay(day: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedule WHERE courseId = :courseId")
    suspend fun getSchedulesByCourse(courseId: Long): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity): Long

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("DELETE FROM schedule WHERE courseId = :courseId")
    suspend fun deleteSchedulesByCourse(courseId: Long)

    @Query("DELETE FROM schedule")
    suspend fun deleteAll()
}