/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.jetbrains.compose.resources.StringResource
import hicwnu.shared.generated.resources.*
import com.chisacwnu.hicwnu.ui.theme.DefaultThemeColor


enum class AutoControlMode(val value: String) {
    
    DND("DND"),

    
    SILENT("SILENT");

    companion object {
        
        fun fromString(value: String?): AutoControlMode {
            return entries.find { it.value == value } ?: DND
        }
    }
}


enum class StartScreen(val value: String, val labelRes: StringResource) {
    
    COURSE_SCHEDULE("COURSE_SCHEDULE", Res.string.nav_course_schedule),

    
    TODAY_SCHEDULE("TODAY_SCHEDULE", Res.string.nav_today_schedule);

    companion object {
        fun fromString(value: String?): StartScreen {
            return entries.find { it.value == value } ?: COURSE_SCHEDULE
        }
    }
}


enum class AppThemeMode(val value: String, val labelRes: StringResource) {
    
    FOLLOW_SYSTEM("FOLLOW_SYSTEM", Res.string.theme_follow_system),

    
    LIGHT("LIGHT", Res.string.theme_light),

    
    DARK("DARK", Res.string.theme_dark);

    companion object {
        fun fromString(value: String?): AppThemeMode? {
            return entries.find { it.value == value }
        }
    }
}


data class AppSettingsModel(
    
    val currentCourseTableId: String = "",

    
    val reminderEnabled: Boolean = false,

    
    val remindBeforeMinutes: Int = 15,

    
    val skippedDates: Set<String> = emptySet(),

    
    val autoModeEnabled: Boolean = false,

    
    val autoControlMode: AutoControlMode = AutoControlMode.DND,

    
    val compatWearableSync: Boolean = false,

    
    val showNonCurrentWeekCourses: Boolean = false,

    
    val startScreen: StartScreen = StartScreen.COURSE_SCHEDULE,

    
    val themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,

    
    val useDynamicColor: Boolean = false,

    
    val customLightPrimary: Long = DefaultThemeColor.toArgb().toLong(),

    
    val customDarkPrimary: Long = DefaultThemeColor.toArgb().toLong(),

    
    val developerModeEnabled: Boolean = false,

    
    val firstLaunchCompleted: Boolean = false,

    
    val defaultsV2Applied: Boolean = false,

    
    val qqGroupNoticeShown: Boolean = false,

    
    val seasonalPresetApplied: String = "",

    val dormSummerWeekdayOff: String = "23:30",
    val dormSummerWeekendOff: String = "00:00",
    val dormWinterWeekdayOff: String = "23:00",
    val dormWinterWeekendOff: String = "23:30",
    val waterMorningStart: String = "06:00",
    val waterMorningEnd: String = "08:30",
    val waterNoonStart: String = "12:00",
    val waterNoonEnd: String = "14:00",
    val waterEveningStart: String = "16:00",
    val showDormLightsOut: Boolean = true,
    val showDormWater: Boolean = true,
) {
    
    companion object {
        val KEY_CURRENT_COURSE_TABLE_ID = stringPreferencesKey("current_course_table_id")
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_REMIND_BEFORE_MINUTES = intPreferencesKey("remind_before_minutes")
        val KEY_SKIPPED_DATES = stringSetPreferencesKey("skipped_dates")
        val KEY_AUTO_MODE_ENABLED = booleanPreferencesKey("auto_mode_enabled")
        val KEY_AUTO_CONTROL_MODE = stringPreferencesKey("auto_control_mode")
        val KEY_COMPAT_WEARABLE_SYNC = booleanPreferencesKey("compat_wearable_sync")
        val KEY_SHOW_NON_CURRENT_WEEK_COURSES = booleanPreferencesKey("show_non_current_week_courses")
        val KEY_START_SCREEN = stringPreferencesKey("start_screen")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val KEY_CUSTOM_LIGHT_PRIMARY = longPreferencesKey("custom_light_primary")
        val KEY_CUSTOM_DARK_PRIMARY = longPreferencesKey("custom_dark_primary")
        val KEY_DEVELOPER_MODE_ENABLED = booleanPreferencesKey("developer_mode_enabled")
        val KEY_FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")
        val KEY_DEFAULTS_V2_APPLIED = booleanPreferencesKey("defaults_v2_applied")
        val KEY_QQ_GROUP_NOTICE_SHOWN = booleanPreferencesKey("qq_group_notice_shown")
        val KEY_SEASONAL_PRESET_APPLIED = stringPreferencesKey("seasonal_preset_applied")
        val KEY_DORM_SUMMER_WEEKDAY_OFF = stringPreferencesKey("dorm_summer_weekday_off")
        val KEY_DORM_SUMMER_WEEKEND_OFF = stringPreferencesKey("dorm_summer_weekend_off")
        val KEY_DORM_WINTER_WEEKDAY_OFF = stringPreferencesKey("dorm_winter_weekday_off")
        val KEY_DORM_WINTER_WEEKEND_OFF = stringPreferencesKey("dorm_winter_weekend_off")
        val KEY_WATER_MORNING_START = stringPreferencesKey("water_morning_start")
        val KEY_WATER_MORNING_END = stringPreferencesKey("water_morning_end")
        val KEY_WATER_NOON_START = stringPreferencesKey("water_noon_start")
        val KEY_WATER_NOON_END = stringPreferencesKey("water_noon_end")
        val KEY_WATER_EVENING_START = stringPreferencesKey("water_evening_start")
        val KEY_SHOW_DORM_LIGHTS_OUT = booleanPreferencesKey("show_dorm_lights_out")
        val KEY_SHOW_DORM_WATER = booleanPreferencesKey("show_dorm_water")

        
        fun fromPreferences(prefs: Preferences, fallbackTableId: String): AppSettingsModel {
            val d = AppSettingsModel()
            return AppSettingsModel(
                currentCourseTableId = prefs[KEY_CURRENT_COURSE_TABLE_ID] ?: fallbackTableId.ifEmpty { d.currentCourseTableId },
                reminderEnabled = prefs[KEY_REMINDER_ENABLED] ?: d.reminderEnabled,
                remindBeforeMinutes = prefs[KEY_REMIND_BEFORE_MINUTES] ?: d.remindBeforeMinutes,
                skippedDates = prefs[KEY_SKIPPED_DATES] ?: d.skippedDates,
                autoModeEnabled = prefs[KEY_AUTO_MODE_ENABLED] ?: d.autoModeEnabled,
                autoControlMode = AutoControlMode.fromString(prefs[KEY_AUTO_CONTROL_MODE]),
                compatWearableSync = prefs[KEY_COMPAT_WEARABLE_SYNC] ?: d.compatWearableSync,
                showNonCurrentWeekCourses = prefs[KEY_SHOW_NON_CURRENT_WEEK_COURSES] ?: d.showNonCurrentWeekCourses,
                startScreen = prefs[KEY_START_SCREEN]?.let { StartScreen.fromString(it) } ?: d.startScreen,
                themeMode = prefs[KEY_THEME_MODE]?.let { AppThemeMode.fromString(it) } ?: d.themeMode,
                useDynamicColor = prefs[KEY_USE_DYNAMIC_COLOR] ?: d.useDynamicColor,
                customLightPrimary = prefs[KEY_CUSTOM_LIGHT_PRIMARY] ?: d.customLightPrimary,
                customDarkPrimary = prefs[KEY_CUSTOM_DARK_PRIMARY] ?: d.customDarkPrimary,
                developerModeEnabled = prefs[KEY_DEVELOPER_MODE_ENABLED] ?: d.developerModeEnabled,
                firstLaunchCompleted = prefs[KEY_FIRST_LAUNCH_COMPLETED] ?: d.firstLaunchCompleted,
                defaultsV2Applied = prefs[KEY_DEFAULTS_V2_APPLIED] ?: d.defaultsV2Applied,
                qqGroupNoticeShown = prefs[KEY_QQ_GROUP_NOTICE_SHOWN] ?: d.qqGroupNoticeShown,
                seasonalPresetApplied = prefs[KEY_SEASONAL_PRESET_APPLIED] ?: d.seasonalPresetApplied,
                dormSummerWeekdayOff = prefs[KEY_DORM_SUMMER_WEEKDAY_OFF] ?: d.dormSummerWeekdayOff,
                dormSummerWeekendOff = prefs[KEY_DORM_SUMMER_WEEKEND_OFF] ?: d.dormSummerWeekendOff,
                dormWinterWeekdayOff = prefs[KEY_DORM_WINTER_WEEKDAY_OFF] ?: d.dormWinterWeekdayOff,
                dormWinterWeekendOff = prefs[KEY_DORM_WINTER_WEEKEND_OFF] ?: d.dormWinterWeekendOff,
                waterMorningStart = prefs[KEY_WATER_MORNING_START] ?: d.waterMorningStart,
                waterMorningEnd = prefs[KEY_WATER_MORNING_END] ?: d.waterMorningEnd,
                waterNoonStart = prefs[KEY_WATER_NOON_START] ?: d.waterNoonStart,
                waterNoonEnd = prefs[KEY_WATER_NOON_END] ?: d.waterNoonEnd,
                waterEveningStart = prefs[KEY_WATER_EVENING_START] ?: d.waterEveningStart,
                showDormLightsOut = prefs[KEY_SHOW_DORM_LIGHTS_OUT] ?: d.showDormLightsOut,
                showDormWater = prefs[KEY_SHOW_DORM_WATER] ?: d.showDormWater,
            )
        }
    }
}