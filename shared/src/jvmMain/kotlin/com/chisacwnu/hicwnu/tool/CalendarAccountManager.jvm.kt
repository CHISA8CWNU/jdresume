/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import kotlinx.datetime.LocalDate

actual object CalendarAccountManager {
    actual suspend fun syncCurrentTableToSystemCalendar(
        courses: List<CourseWithWeeks>,
        timeSlots: List<TimeSlot>,
        semesterStartDate: LocalDate,
        semesterTotalWeeks: Int,
        firstDayOfWeekInt: Int,
        alarmMinutes: Int?,
        skippedDates: Set<String>?
    ): Boolean {
        return false
    }
}