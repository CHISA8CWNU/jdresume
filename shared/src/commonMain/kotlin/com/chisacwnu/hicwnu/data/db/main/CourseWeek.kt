/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index


@Entity(
    tableName = "course_weeks",
        primaryKeys = ["courseId", "weekNumber"],
        foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
        indices = [Index(value = ["courseId"]), Index(value = ["weekNumber"])]
)
data class CourseWeek(
    val courseId: String,     val weekNumber: Int   )