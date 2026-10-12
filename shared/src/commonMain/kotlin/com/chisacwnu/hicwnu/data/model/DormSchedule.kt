/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import com.chisacwnu.hicwnu.ui.settings.time.SchedulePreset
import com.chisacwnu.hicwnu.ui.settings.time.schedulePresetForDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

data class WaterSegment(
    val start: String,
    val end: String
)

object DormSchedule {

    fun lightsOut(settings: AppSettingsModel, date: LocalDate): String {
        val preset = schedulePresetForDate(date.monthNumber, date.dayOfMonth)

        val isWeekend = date.dayOfWeek.isoDayNumber in 5..6
        return when (preset) {
            SchedulePreset.WINTER ->
                if (isWeekend) settings.dormWinterWeekendOff else settings.dormWinterWeekdayOff

            SchedulePreset.SUMMER ->
                if (isWeekend) settings.dormSummerWeekendOff else settings.dormSummerWeekdayOff
        }
    }

    fun waterSegments(settings: AppSettingsModel, lightsOut: String): List<WaterSegment> {
        return listOf(
            WaterSegment(settings.waterMorningStart, settings.waterMorningEnd),
            WaterSegment(settings.waterNoonStart, settings.waterNoonEnd),
            WaterSegment(settings.waterEveningStart, lightsOut)
        )
    }
}
