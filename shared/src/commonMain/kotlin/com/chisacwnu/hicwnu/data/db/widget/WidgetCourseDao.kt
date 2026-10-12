/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.widget

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetCourseDao {
    
    
    @Query("SELECT * FROM widget_courses WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, startTime ASC")
    fun getWidgetCoursesByDateRange(startDate: String, endDate: String): Flow<List<WidgetCourse>>

    
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(courses: List<WidgetCourse>)

    
    @Query("DELETE FROM widget_courses")
    suspend fun deleteAll()
}