/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.db.main.CourseTableConfig
import com.chisacwnu.hicwnu.data.model.AppSettingsModel
import com.chisacwnu.hicwnu.data.model.AppThemeMode
import com.chisacwnu.hicwnu.data.model.StartScreen
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.TimeSlotRepository
import com.chisacwnu.hicwnu.ui.settings.time.SUMMER_SLOTS
import com.chisacwnu.hicwnu.ui.settings.time.SchedulePreset
import com.chisacwnu.hicwnu.ui.settings.time.WINTER_SLOTS
import com.chisacwnu.hicwnu.ui.settings.time.schedulePresetForDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Clock
import kotlin.time.Instant


data class SettingsUiState(
    val appSettings: AppSettingsModel = AppSettingsModel(),
    val courseConfig: CourseTableConfig? = null,
    val currentWeek: Int? = null,
    val isReady: Boolean = false
)

@KoinViewModel
class SettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val timeSlotRepository: TimeSlotRepository
) : ViewModel() {

        private val appSettingsFlow = appSettingsRepository.getAppSettings()

        @OptIn(ExperimentalCoroutinesApi::class)
    private val courseTableConfigFlow = appSettingsFlow.flatMapLatest { settings ->
        val id = settings.currentCourseTableId
        if (id.isNotEmpty()) appSettingsRepository.getCourseTableConfigFlow(id)
        else flowOf(null)
    }

    
    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsFlow,
        courseTableConfigFlow
    ) { settings, config ->
        val week = if (config != null) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val rawWeek = appSettingsRepository.getWeekIndexAtDate(
                targetDate = today,
                startDateStr = config.semesterStartDate,
                firstDayOfWeekInt = config.firstDayOfWeek
            )
            rawWeek?.takeIf { it in 1..config.semesterTotalWeeks }
        } else null

        SettingsUiState(
            appSettings = settings,
            courseConfig = config,
            currentWeek = week,
            isReady = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = SettingsUiState()
    )

    
    fun onShowNonCurrentWeekChanged(show: Boolean) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(showNonCurrentWeekCourses = show)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    
    fun onShowWeekendsChanged(show: Boolean) {
        viewModelScope.launch {
            uiState.value.courseConfig?.let { currentConfig ->
                val update = if (!show) {
                    currentConfig.copy(showWeekends = false, firstDayOfWeek = DayOfWeek.MONDAY.isoDayNumber)
                } else {
                    currentConfig.copy(showWeekends = true)
                }
                appSettingsRepository.insertOrUpdateCourseConfig(update)
            }
        }
    }

    
    fun onSemesterStartDateSelected(selectedDateMillis: Long?) {
        viewModelScope.launch {
            val dateMillis = selectedDateMillis ?: return@launch
            uiState.value.courseConfig?.let { currentConfig ->
                val selectedDate = Instant.fromEpochMilliseconds(dateMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val newConfig = currentConfig.copy(
                    semesterStartDate = selectedDate.toString()
                )
                appSettingsRepository.insertOrUpdateCourseConfig(newConfig)
            }
        }
    }

    
    fun onSemesterTotalWeeksSelected(totalWeeks: Int) {
        viewModelScope.launch {
            uiState.value.courseConfig?.let {
                appSettingsRepository.insertOrUpdateCourseConfig(it.copy(semesterTotalWeeks = totalWeeks))
            }
        }
    }

    
    fun onCurrentWeekManuallySet(weekNumber: Int?) {
        viewModelScope.launch {
            appSettingsRepository.setSemesterStartDateFromWeek(weekNumber)
        }
    }

    
    fun onFirstDayOfWeekSelected(dayOfWeekInt: Int) {
        viewModelScope.launch {
            uiState.value.courseConfig?.let { currentConfig ->
                appSettingsRepository.insertOrUpdateCourseConfig(currentConfig.copy(firstDayOfWeek = dayOfWeekInt))
            }
        }
    }

    
    fun onForceCourseConfigDefaults() {
        viewModelScope.launch {
            val settings = appSettingsRepository.getAppSettingsOnce()
            if (!settings.defaultsV2Applied) {
                appSettingsRepository.setDefaultsV2Applied(true)
                val v2TableId = settings.currentCourseTableId
                if (v2TableId.isNotEmpty()) {
                    val v2Config = appSettingsRepository.getCourseConfigOnce(v2TableId)
                    if (v2Config != null && !v2Config.showWeekends) {
                        appSettingsRepository.insertOrUpdateCourseConfig(v2Config.copy(showWeekends = true))
                    }
                }
            }
            val tableId = settings.currentCourseTableId
            if (tableId.isEmpty()) return@launch
                        applySeasonalTimeSlots(tableId)
            val config = appSettingsRepository.getCourseConfigOnce(tableId) ?: return@launch
            if (config.semesterTotalWeeks != 20 || config.firstDayOfWeek != DayOfWeek.MONDAY.isoDayNumber) {
                appSettingsRepository.insertOrUpdateCourseConfig(config)
            }
        }
    }

    
    private suspend fun applySeasonalTimeSlots(tableId: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val preset = schedulePresetForDate(now.monthNumber, now.dayOfMonth)
        val settings = appSettingsRepository.getAppSettingsOnce()
        if (settings.seasonalPresetApplied == preset.name) return
        val slots = if (preset == SchedulePreset.WINTER) WINTER_SLOTS else SUMMER_SLOTS
        timeSlotRepository.replaceAllForCourseTable(
            tableId,
            slots.map { it.copy(courseTableId = tableId) }
        )
        val config = appSettingsRepository.getCourseConfigOnce(tableId)
            ?: CourseTableConfig(courseTableId = tableId)
        appSettingsRepository.insertOrUpdateCourseConfig(
            config.copy(defaultClassDuration = 40, defaultBreakDuration = 10)
        )
        appSettingsRepository.setSeasonalPresetApplied(preset.name)
    }

    
    fun onStartScreenChanged(newScreen: StartScreen) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(startScreen = newScreen)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    
    fun onThemeModeChanged(newMode: AppThemeMode) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(themeMode = newMode)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    
    fun onUseDynamicColorChanged(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(useDynamicColor = enabled)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onDormSummerWeekdayOffChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(dormSummerWeekdayOff = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onDormSummerWeekendOffChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(dormSummerWeekendOff = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onDormWinterWeekdayOffChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(dormWinterWeekdayOff = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onDormWinterWeekendOffChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(dormWinterWeekendOff = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onWaterMorningStartChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(waterMorningStart = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onWaterMorningEndChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(waterMorningEnd = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onWaterNoonStartChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(waterNoonStart = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onWaterNoonEndChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(waterNoonEnd = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onWaterEveningStartChanged(value: String) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(waterEveningStart = value)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onShowDormLightsOutChanged(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(showDormLightsOut = enabled)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    fun onShowDormWaterChanged(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(showDormWater = enabled)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }


    
    fun onCustomLightPrimaryChanged(color: Color? = null) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val newColorArgb = color?.toArgb()?.toLong()
                ?: AppSettingsModel().customLightPrimary

            val updatedSettings = currentSettings.copy(
                customLightPrimary = newColorArgb
            )
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    
    fun onCustomDarkPrimaryChanged(color: Color? = null) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val newColorArgb = color?.toArgb()?.toLong()
                ?: AppSettingsModel().customDarkPrimary

            val updatedSettings = currentSettings.copy(
                customDarkPrimary = newColorArgb
            )
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    
    fun onDeveloperModeChanged(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = uiState.value.appSettings
            val updatedSettings = currentSettings.copy(developerModeEnabled = enabled)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)
        }
    }

    
    fun onFirstLaunchCompleted() {
        viewModelScope.launch {
            if (uiState.value.appSettings.firstLaunchCompleted) return@launch
            appSettingsRepository.setFirstLaunchCompleted(true)
        }
    }

    
    fun onQqGroupNoticeClosed() {
        viewModelScope.launch {
            if (uiState.value.appSettings.qqGroupNoticeShown) return@launch
            appSettingsRepository.setQqGroupNoticeShown(true)
        }
    }
}