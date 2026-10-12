/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RoomWarnings
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    
    @Transaction
    @Query(
        """
        SELECT * FROM courses 
        WHERE courseTableId = :courseTableId 
        ORDER BY 
            day ASC, 
            CASE WHEN isCustomTime = 0 THEN startSection ELSE 99 END ASC,  
            CASE WHEN isCustomTime = 1 THEN customStartTime ELSE '99:99' END ASC
        """
    )
    @Suppress(RoomWarnings.QUERY_MISMATCH)
    fun getCoursesWithWeeksByTableId(courseTableId: String): Flow<List<CourseWithWeeks>>

    
    @Query("SELECT EXISTS(SELECT 1 FROM courses WHERE id = :courseId)")
    suspend fun exists(courseId: String): Boolean

    
    @Update
    suspend fun update(course: Course)

    
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(courses: List<Course>)

    
    @Delete
    suspend fun delete(course: Course)

    
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: String)

    
    @Query("DELETE FROM courses WHERE id IN (:courseIds)")
    suspend fun deleteCoursesByIds(courseIds: List<String>)

    
    @Query("DELETE FROM courses WHERE courseTableId = :courseTableId")
    suspend fun deleteCoursesByTableId(courseTableId: String)

    
    @Transaction
    @Query(
        """
        SELECT * FROM courses AS c
        INNER JOIN course_weeks AS cw ON c.id = cw.courseId
        WHERE c.courseTableId = :courseTableId
          AND c.day = :day
          AND cw.weekNumber = :weekNumber
        ORDER BY 
            CASE WHEN c.isCustomTime = 0 THEN c.startSection ELSE 99 END ASC,  
            CASE WHEN c.isCustomTime = 1 THEN c.customStartTime ELSE '99:99' END ASC
        """
    )
    @Suppress(RoomWarnings.QUERY_MISMATCH)
    fun getCoursesWithWeeksByDayAndWeek(
        courseTableId: String,
        day: Int,
        weekNumber: Int
    ): Flow<List<CourseWithWeeks>>

    
    @Query("UPDATE courses SET colorInt = :newColorInt WHERE id = :courseId")
    suspend fun updateCourseColorById(courseId: String, newColorInt: Int)

    
    @Query("DELETE FROM courses WHERE courseTableId = :tableId AND name IN (:courseNames)")
    suspend fun deleteCoursesByNames(tableId: String, courseNames: List<String>)

    
    @Transaction
    @Query(
        """
        SELECT DISTINCT c.* FROM courses AS c
        INNER JOIN course_weeks AS cw ON c.id = cw.courseId
        WHERE c.courseTableId = :courseTableId
          AND cw.weekNumber = :weekNumber
        ORDER BY 
            c.day ASC,
            CASE WHEN c.isCustomTime = 0 THEN c.startSection ELSE 99 END ASC,  
            CASE WHEN c.isCustomTime = 1 THEN c.customStartTime ELSE '99:99' END ASC
        """
    )
    @Suppress(RoomWarnings.QUERY_MISMATCH)
    fun getCoursesWithWeeksByTableAndWeek(
        courseTableId: String,
        weekNumber: Int
    ): Flow<List<CourseWithWeeks>>
}