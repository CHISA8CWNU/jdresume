/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Embedded
import androidx.room3.Relation


data class CourseWithWeeks(
    @Embedded
    val course: Course,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["courseId"]
    )
    val weeks: List<CourseWeek>
)