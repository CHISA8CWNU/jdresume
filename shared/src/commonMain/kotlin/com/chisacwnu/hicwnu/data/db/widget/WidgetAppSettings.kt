/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.widget

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

@Entity(tableName = "widget_semester_start_date")
data class WidgetAppSettings(
    @PrimaryKey
    val id: Int = 1,
    val semesterStartDate: String? = null,      val semesterTotalWeeks: Int = 20,     val firstDayOfWeek: Int = DayOfWeek.MONDAY.isoDayNumber )