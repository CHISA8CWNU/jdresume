/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseTableConfigDao {

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: CourseTableConfig)

    
    @Query("SELECT * FROM course_table_config WHERE courseTableId = :courseTableId")
    fun getConfigById(courseTableId: String): Flow<CourseTableConfig?>

    
    @Query("SELECT * FROM course_table_config WHERE courseTableId = :courseTableId LIMIT 1")
    suspend fun getConfigOnce(courseTableId: String): CourseTableConfig?
}