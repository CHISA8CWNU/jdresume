/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseWeekDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(courseWeeks: List<CourseWeek>)

    
    @Transaction
    suspend fun updateCourseWeeks(courseId: String, courseWeeks: List<CourseWeek>) {
                deleteByCourseId(courseId)
                insertAll(courseWeeks)
    }

    
    @Query("DELETE FROM course_weeks WHERE courseId = :courseId")
    suspend fun deleteByCourseId(courseId: String)

    
    @Query("DELETE FROM course_weeks WHERE courseId IN (:courseIds) AND weekNumber = :weekNumber")
    suspend fun deleteCourseWeeksForCourseAndWeek(courseIds: List<String>, weekNumber: Int)
}