/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL


val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `course_table_config` (
                `courseTableId` TEXT NOT NULL,
                `showWeekends` INTEGER NOT NULL DEFAULT 0,
                `semesterStartDate` TEXT,
                `semesterTotalWeeks` INTEGER NOT NULL DEFAULT 20,
                `defaultClassDuration` INTEGER NOT NULL DEFAULT 45,
                `defaultBreakDuration` INTEGER NOT NULL DEFAULT 10,
                `firstDayOfWeek` INTEGER NOT NULL DEFAULT 1, 
                PRIMARY KEY(`courseTableId`),
                FOREIGN KEY(`courseTableId`) REFERENCES `course_tables`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """
        )
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_course_table_config_courseTableId` ON `course_table_config` (`courseTableId`)")

        
        var currentCourseTableId: String? = null
        var showWeekends = 0
        var semesterStartDate: String? = null
        var semesterTotalWeeks = 20
        var defaultClassDuration = 45
        var defaultBreakDuration = 10

                connection.prepare(
            "SELECT currentCourseTableId, showWeekends, semesterStartDate, semesterTotalWeeks, defaultClassDuration, defaultBreakDuration FROM app_settings WHERE id = 1 LIMIT 1"
        ).use { stmt ->
            if (stmt.step()) {
                currentCourseTableId = if (!stmt.isNull(0)) stmt.getText(0) else null
                showWeekends = stmt.getLong(1).toInt()
                semesterStartDate = if (!stmt.isNull(2)) stmt.getText(2) else null
                semesterTotalWeeks = stmt.getLong(3).toInt()
                defaultClassDuration = stmt.getLong(4).toInt()
                defaultBreakDuration = stmt.getLong(5).toInt()
            }
        }

                if (currentCourseTableId != null) {
            connection.prepare(
                """
                INSERT INTO `course_table_config` (courseTableId, showWeekends, semesterStartDate, semesterTotalWeeks, defaultClassDuration, defaultBreakDuration, firstDayOfWeek)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """
            ).use { stmt ->
                stmt.bindText(1, currentCourseTableId)
                stmt.bindLong(2, showWeekends.toLong())
                if (semesterStartDate != null) {
                    stmt.bindText(3, semesterStartDate)
                } else {
                    stmt.bindNull(3)
                }
                stmt.bindLong(4, semesterTotalWeeks.toLong())
                stmt.bindLong(5, defaultClassDuration.toLong())
                stmt.bindLong(6, defaultBreakDuration.toLong())
                stmt.bindLong(7, 1L)
                stmt.step()
            }
        }

                        connection.execSQL("ALTER TABLE app_settings RENAME TO app_settings_old")

                connection.execSQL(
            """
            CREATE TABLE `app_settings` (
                `id` INTEGER NOT NULL,
                `currentCourseTableId` TEXT,
                `reminderEnabled` INTEGER NOT NULL DEFAULT 0,
                `remindBeforeMinutes` INTEGER NOT NULL DEFAULT 15,
                `skippedDates` TEXT,
                `autoModeEnabled` INTEGER NOT NULL DEFAULT 0,
                `autoControlMode` TEXT NOT NULL DEFAULT 'DND',
                PRIMARY KEY(`id`)
            )
            """
        )

                connection.execSQL(
            """
            INSERT INTO app_settings (id, currentCourseTableId, reminderEnabled, remindBeforeMinutes, skippedDates, autoModeEnabled, autoControlMode)
            SELECT id, currentCourseTableId, reminderEnabled, remindBeforeMinutes, skippedDates, 0, 'DND' FROM app_settings_old
            """
        )

                connection.execSQL("DROP TABLE app_settings_old")
    }
}


val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {

                connection.execSQL(
            """
            CREATE TABLE `courses_new` (
                `id` TEXT NOT NULL,
                `courseTableId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `teacher` TEXT NOT NULL,
                `position` TEXT NOT NULL,
                `day` INTEGER NOT NULL,
                `startSection` INTEGER,  -- 变为可空 (Int?)
                `endSection` INTEGER,    -- 变为可空 (Int?)
                `isCustomTime` INTEGER NOT NULL DEFAULT 0, -- 新增字段，默认 FALSE
                `customStartTime` TEXT,  -- 新增字段
                `customEndTime` TEXT,    -- 新增字段
                `colorInt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`courseTableId`) REFERENCES `course_tables`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """
        )

                connection.execSQL(
            """
            INSERT INTO courses_new (id, courseTableId, name, teacher, position, day, startSection, endSection, colorInt, isCustomTime, customStartTime, customEndTime)
            SELECT id, courseTableId, name, teacher, position, day, startSection, endSection, colorInt, 0, NULL, NULL
            FROM courses
            """
        )

                connection.execSQL("DROP TABLE courses")
        connection.execSQL("ALTER TABLE courses_new RENAME TO courses")

                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_courseTableId` ON `courses` (`courseTableId`)")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
)