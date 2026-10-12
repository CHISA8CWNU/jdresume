/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.cbor.Cbor

object CourseImportExport {

    
    const val COURSE_SCHEMA_VERSION = 1

    
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    
    @OptIn(ExperimentalSerializationApi::class)
    val cbor = Cbor {
        ignoreUnknownKeys = true
    }

    
    @Serializable
    data class TotalAppBackupEnvelope(
        val backupTimestamp: Long,                  val appVersionCode: Int,                    val currentCourseTableId: String,           val allTables: List<SingleTablePack>     )

    
    @Serializable
    data class SingleTablePack(
        val tableId: String,                  val tableName: String,                val createdAt: Long,                  val tableData: CourseTableExportModel     )



        @Serializable
    data class CourseConfigJsonModel(
        val semesterStartDate: String? = null,
        val semesterTotalWeeks: Int = 20,
        val defaultClassDuration: Int = 45,
        val defaultBreakDuration: Int = 10,
        val firstDayOfWeek: Int = 1
    )

        @Serializable
    data class CourseTableImportModel(
        val courses: List<ImportCourseJsonModel>,
        val timeSlots: List<TimeSlotJsonModel>? = emptyList(),
        val config: CourseConfigJsonModel? = null
    )

    @Serializable
    data class ImportCourseJsonModel(
        val id: String? = null,
        val name: String,
        val teacher: String,
        val position: String,
        val day: Int,
        val startSection: Int? = null,
        val endSection: Int? = null,
        val weeks: List<Int>,
        val isCustomTime: Boolean = false,
        val customStartTime: String? = null,
        val customEndTime: String? = null,
        val color: Int? = null,
        val remark: String? = null
    )

        @Serializable
    data class CourseTableExportModel(
        val courses: List<ExportCourseJsonModel>,
        val timeSlots: List<TimeSlotJsonModel>,
        val config: CourseConfigJsonModel
    )

    @Serializable
    data class ExportCourseJsonModel(
        val id: String,         val name: String,
        val teacher: String,
        val position: String,
        val day: Int,
        val startSection: Int? = null,
        val endSection: Int? = null,
        val color: Int,         val weeks: List<Int>,
        val isCustomTime: Boolean = false,
        val customStartTime: String? = null,
        val customEndTime: String? = null,
        val remark: String?
    )

        @Serializable
    data class TimeSlotJsonModel(
        val number: Int,
        val startTime: String,
        val endTime: String,
        val alias: String? = null
    )

}