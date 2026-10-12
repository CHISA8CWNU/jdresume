/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.widget

import androidx.room3.Entity
import androidx.room3.PrimaryKey


@Entity(tableName = "widget_courses")
data class WidgetCourse(
    @PrimaryKey
    val id: String,     val name: String,     val teacher: String,     val position: String,     val startTime: String,     val endTime: String,     val isSkipped: Boolean = false,     val date: String,     val colorInt: Int )