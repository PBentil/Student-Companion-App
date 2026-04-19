package com.example.studentcompanion.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, createdAt DESC")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = :completed ORDER BY createdAt DESC")
    suspend fun getTasksByCompletion(completed: Boolean): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompleted()

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}