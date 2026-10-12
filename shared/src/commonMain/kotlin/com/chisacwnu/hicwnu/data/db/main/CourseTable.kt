/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Entity
import androidx.room3.PrimaryKey


@Entity(tableName = "course_tables")
data class CourseTable(
    @PrimaryKey
    val id: String,     val name: String,     val createdAt: Long )