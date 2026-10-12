/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseTableDao {
    
    @Query("SELECT * FROM course_tables ORDER BY createdAt DESC")
    fun getAllCourseTables(): Flow<List<CourseTable>>

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(courseTable: CourseTable)

    
    @Update
    suspend fun update(courseTable: CourseTable)

    
    @Delete
    suspend fun delete(courseTable: CourseTable)

    
    @Query("DELETE FROM course_tables WHERE id = :tableId")
    suspend fun deleteById(tableId: String)

    
    @Query("SELECT * FROM course_tables ORDER BY createdAt ASC LIMIT 1")
    suspend fun getFirstTableOnce(): CourseTable?
}