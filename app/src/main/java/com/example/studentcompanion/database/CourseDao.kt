package com.example.studentcompanion.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY courseCode ASC")
    fun getAllCourses(): LiveData<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY courseCode ASC")
    suspend fun getAllCoursesSync(): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()
}