/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.data.db.main.TimeSlotDao
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single


@Single
class TimeSlotRepository(
    private val timeSlotDao: TimeSlotDao
) {
    
    fun getTimeSlotsByCourseTableId(courseTableId: String): Flow<List<TimeSlot>> {
        return timeSlotDao.getTimeSlotsByCourseTableId(courseTableId)
    }

    
    suspend fun insertAll(timeSlots: List<TimeSlot>) {
        timeSlotDao.insertAll(timeSlots)
    }

    
    suspend fun replaceAllForCourseTable(courseTableId: String, timeSlots: List<TimeSlot>) {
        timeSlotDao.deleteAllTimeSlotsByCourseTableId(courseTableId)
        if (timeSlots.isNotEmpty()) {
            timeSlotDao.insertAll(timeSlots)
        }
    }
}