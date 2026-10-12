/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index


@Entity(
    tableName = "time_slots",
        foreignKeys = [
        ForeignKey(
            entity = CourseTable::class,
            parentColumns = ["id"],
            childColumns = ["courseTableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseTableId"])],
        primaryKeys = ["number", "courseTableId"]
)
data class TimeSlot(
    val number: Int,     val startTime: String,     val endTime: String,     val courseTableId: String,     val alias: String? = null )