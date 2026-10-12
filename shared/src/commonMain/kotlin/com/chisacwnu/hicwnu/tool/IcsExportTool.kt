/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

import com.chisacwnu.hicwnu.data.db.main.Course
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.getString
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.course_teacher_prefix
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


object IcsExportTool {

    
    inline fun processCourseInstances(
        courses: List<CourseWithWeeks>,
        timeSlots: List<TimeSlot>,
        semesterStartDate: LocalDate,
        semesterTotalWeeks: Int,
        firstDayOfWeekInt: Int,
        skippedDates: Set<String>? = null,
        action: (course: Course, startDateTime: LocalDateTime, endDateTime: LocalDateTime, weekNumber: Int) -> Unit
    ) {
        val timeSlotMap = timeSlots.associateBy { it.number }
        val dayOfWeekMap = mapOf(
            1 to DayOfWeek.MONDAY,
            2 to DayOfWeek.TUESDAY,
            3 to DayOfWeek.WEDNESDAY,
            4 to DayOfWeek.THURSDAY,
            5 to DayOfWeek.FRIDAY,
            6 to DayOfWeek.SATURDAY,
            7 to DayOfWeek.SUNDAY
        )

        val firstDayOfWeek = dayOfWeekMap[firstDayOfWeekInt] ?: DayOfWeek.MONDAY

                val diff = (semesterStartDate.dayOfWeek.isoDayNumber - firstDayOfWeek.isoDayNumber + 7) % 7
        val alignedSemesterStart = semesterStartDate.minus(diff.toLong(), DateTimeUnit.DAY)

        courses.forEach { courseWithWeeks ->
            val course = courseWithWeeks.course
            val weeks = courseWithWeeks.weeks.map { it.weekNumber }

            val startTime: LocalTime
            val endTime: LocalTime
            if (course.isCustomTime) {
                val s = course.customStartTime ?: return@forEach
                val e = course.customEndTime ?: return@forEach
                try {
                    startTime = LocalTime.parse(s)
                    endTime = LocalTime.parse(e)
                } catch (_: Exception) { return@forEach }
            } else {
                val s = timeSlotMap[course.startSection]?.startTime ?: return@forEach
                val e = timeSlotMap[course.endSection]?.endTime ?: return@forEach
                try {
                    startTime = LocalTime.parse(s)
                    endTime = LocalTime.parse(e)
                } catch (_: Exception) { return@forEach }
            }

            val dayOfWeek = dayOfWeekMap[course.day] ?: return@forEach

            weeks.forEach { week ->
                val dayOffset = (dayOfWeek.isoDayNumber - firstDayOfWeek.isoDayNumber + 7) % 7
                val date = alignedSemesterStart
                    .plus((week - 1).toLong(), DateTimeUnit.WEEK)
                    .plus(dayOffset.toLong(), DateTimeUnit.DAY)

                val weekIndex = alignedSemesterStart.daysUntil(date) / 7 + 1
                if (weekIndex > semesterTotalWeeks) return@forEach

                if (skippedDates?.contains(date.toString()) == true) return@forEach

                action(
                    course,
                    LocalDateTime(date, startTime),
                    LocalDateTime(date, endTime),
                    week
                )
            }
        }
    }

    
    suspend fun generateIcsFileContent(
        courses: List<CourseWithWeeks>,
        timeSlots: List<TimeSlot>,
        semesterStartDate: LocalDate,
        semesterTotalWeeks: Int,
        firstDayOfWeekInt: Int,
        alarmMinutes: Int? = null,
        skippedDates: Set<String>? = null
    ): String {
        val ics = StringBuilder()

                ics.append("BEGIN:VCALENDAR\r\n")
        ics.append("VERSION:2.0\r\n")
        ics.append("PRODID:-//HiCWNU//ZH\r\n")
        ics.append("CALSCALE:GREGORIAN\r\n")
        ics.append("METHOD:PUBLISH\r\n")
        ics.append("BEGIN:VTIMEZONE\r\n")
        ics.append("TZID:Asia/Shanghai\r\n")
        ics.append("BEGIN:STANDARD\r\n")
        ics.append("DTSTART:19700101T000000Z\r\n")
        ics.append("TZOFFSETFROM:+0800\r\n")
        ics.append("TZOFFSETTO:+0800\r\n")
        ics.append("END:STANDARD\r\n")
        ics.append("END:VTIMEZONE\r\n")

        val dtStampStr = formatDateTimeUtc(Clock.System.now())

        processCourseInstances(
            courses, timeSlots, semesterStartDate, semesterTotalWeeks, firstDayOfWeekInt, skippedDates
        ) { course, start, end, _ ->
            ics.append("BEGIN:VEVENT\r\n")
            ics.append("UID:${generateUid()}@cwnu.edu.cn\r\n")
            ics.append("DTSTAMP:$dtStampStr\r\n")
            ics.append("DTSTART;TZID=Asia/Shanghai:${formatDateTimeLocal(start)}\r\n")
            ics.append("DTEND;TZID=Asia/Shanghai:${formatDateTimeLocal(end)}\r\n")
            ics.append("SUMMARY:${escapeText(course.name)}\r\n")

            if (course.position.isNotBlank()) {
                ics.append("LOCATION:${escapeText(course.position)}\r\n")
            }

            if (course.teacher.isNotBlank()) {
                val teacherDescription = getString(Res.string.course_teacher_prefix, course.teacher)
                ics.append("DESCRIPTION:${escapeText(teacherDescription)}\r\n")
            }

            if (alarmMinutes != null && alarmMinutes in 0..60) {
                ics.append("BEGIN:VALARM\r\n")
                ics.append("ACTION:DISPLAY\r\n")
                ics.append("DESCRIPTION:课程提醒\r\n")
                ics.append("TRIGGER:-PT${alarmMinutes}M\r\n")
                ics.append("END:VALARM\r\n")
            }
            ics.append("END:VEVENT\r\n")
        }

        ics.append("END:VCALENDAR\r\n")
        return ics.toString()
    }

    
    private fun formatDateTimeLocal(dateTime: LocalDateTime): String {
        val y = dateTime.year.toString().padStart(4, '0')
        val m = dateTime.month.number.toString().padStart(2, '0')
        val d = dateTime.day.toString().padStart(2, '0')
        val h = dateTime.hour.toString().padStart(2, '0')
        val min = dateTime.minute.toString().padStart(2, '0')
        val s = dateTime.second.toString().padStart(2, '0')
        return "${y}${m}${d}T${h}${min}${s}"
    }

    
    private fun formatDateTimeUtc(instant: Instant): String {
        return instant.toString().replace("-", "").replace(":", "").substringBefore(".") + "Z"
    }

    
    @OptIn(ExperimentalUuidApi::class)
    private fun generateUid(): String {
        return Uuid.random().toString()
    }

    
    private fun escapeText(text: String): String {
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")
    }
}