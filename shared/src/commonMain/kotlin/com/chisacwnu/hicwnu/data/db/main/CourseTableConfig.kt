/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber


@Entity(
    tableName = "course_table_config",
    foreignKeys = [
        ForeignKey(
            entity = CourseTable::class,
            parentColumns = ["id"],
            childColumns = ["courseTableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["courseTableId"], unique = true)
    ]
)
data class CourseTableConfig(
    @PrimaryKey
    val courseTableId: String,
    val showWeekends: Boolean = true,     val semesterStartDate: String? = null,     val semesterTotalWeeks: Int = 20,
    val defaultClassDuration: Int = 45,     val defaultBreakDuration: Int = 10,
    val firstDayOfWeek: Int = DayOfWeek.MONDAY.isoDayNumber )