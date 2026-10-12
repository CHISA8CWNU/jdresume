/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey


@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(
        entity = CourseTable::class,
        parentColumns = ["id"],
        childColumns = ["courseTableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["courseTableId"])]
)
data class Course(
    @PrimaryKey
    val id: String,     val courseTableId: String,     val name: String,     val teacher: String,     val position: String,     val day: Int,
        val startSection: Int?,     val endSection: Int?,       val isCustomTime: Boolean = false,     val customStartTime: String?,     val customEndTime: String?,       val colorInt: Int,     val remark: String? = null, )