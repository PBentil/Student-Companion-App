package com.example.studentcompanion.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): LiveData<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    suspend fun getAllAssignmentsSync(): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE status = :status ORDER BY dueDate ASC")
    suspend fun getAssignmentsByStatus(status: String): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE isCompleted = :completed ORDER BY dueDate ASC")
    suspend fun getAssignmentsByCompletion(completed: Boolean): List<AssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: AssignmentEntity): Long

    @Update
    suspend fun update(assignment: AssignmentEntity)

    @Delete
    suspend fun delete(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()
}