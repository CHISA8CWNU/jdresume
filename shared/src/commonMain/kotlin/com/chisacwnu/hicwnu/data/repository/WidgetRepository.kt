/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import com.chisacwnu.hicwnu.data.db.widget.WidgetAppSettings
import com.chisacwnu.hicwnu.data.db.widget.WidgetAppSettingsDao
import com.chisacwnu.hicwnu.data.db.widget.WidgetCourse
import com.chisacwnu.hicwnu.data.db.widget.WidgetCourseDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
class WidgetRepository(
    private val widgetCourseDao: WidgetCourseDao,
    private val widgetAppSettingsDao: WidgetAppSettingsDao
) {

    
    fun getWidgetCoursesByDateRange(startDate: String, endDate: String): Flow<List<WidgetCourse>> {
        return widgetCourseDao.getWidgetCoursesByDateRange(startDate, endDate)
    }

    
    suspend fun insertAll(courses: List<WidgetCourse>) {
        widgetCourseDao.insertAll(courses)
    }

    
    suspend fun deleteAll() {
        widgetCourseDao.deleteAll()
    }

    
    suspend fun insertOrUpdateAppSettings(settings: WidgetAppSettings) {
        widgetAppSettingsDao.insertOrUpdate(settings)
    }

    
    
    fun getCurrentWeekFlow(): Flow<Int?> {
        return widgetAppSettingsDao.getAppSettings()
            .map { settings ->
                val totalWeeks = settings?.semesterTotalWeeks ?: 0
                val startDate = settings?.semesterStartDate
                val firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.MONDAY.isoDayNumber

                calculateCurrentWeek(startDate, totalWeeks, firstDayOfWeek)
            }
    }

    
    private fun calculateCurrentWeek(semesterStartDateStr: String?, totalWeeks: Int, firstDayOfWeekInt: Int): Int? {
        if (semesterStartDateStr.isNullOrEmpty() || totalWeeks <= 0) return null

        return try {
            val startDate = LocalDate.parse(semesterStartDateStr)
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            val alignedStartDate = getStartDayOfWeek(startDate, firstDayOfWeekInt)
            val alignedToday = getStartDayOfWeek(today, firstDayOfWeekInt)

            if (alignedToday < alignedStartDate) return null

            val diffDays = alignedStartDate.daysUntil(alignedToday)
            val diffWeeks = diffDays / 7
            val calculatedWeek = diffWeeks + 1

            if (calculatedWeek in 1..totalWeeks) calculatedWeek else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    
    private fun getStartDayOfWeek(date: LocalDate, firstDayOfWeekInt: Int): LocalDate {
        val targetFirstDay = DayOfWeek(firstDayOfWeekInt)
        var current = date
        while (current.dayOfWeek != targetFirstDay) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return current
    }
}