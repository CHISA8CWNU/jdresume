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
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {
    
    @Query("SELECT * FROM time_slots WHERE courseTableId = :courseTableId ORDER BY number ASC")
    fun getTimeSlotsByCourseTableId(courseTableId: String): Flow<List<TimeSlot>>

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timeSlots: List<TimeSlot>)

    
    @Update
    suspend fun update(timeSlot: TimeSlot)

    
    @Delete
    suspend fun delete(timeSlot: TimeSlot)

    
    @Query("DELETE FROM time_slots WHERE courseTableId = :courseTableId")
    suspend fun deleteAllTimeSlotsByCourseTableId(courseTableId: String)
}