/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.db.main.CourseTableConfig
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.CourseTableRepository
import com.chisacwnu.hicwnu.data.repository.TimeSlotRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class TimeSlotViewModel(
    private val timeSlotRepository: TimeSlotRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val courseTableRepository: CourseTableRepository
) : ViewModel() {

        private val appSettingsFlow = appSettingsRepository.getAppSettings()

        private var initialTimeSlots: List<TimeSlot> = emptyList()
    private var initialClassDuration: Int = 45
    private var initialBreakDuration: Int = 10
    private var isDataInitialized = false

    
    @OptIn(ExperimentalCoroutinesApi::class)
    val timeSlotsUiState: StateFlow<TimeSlotUiState> =
        appSettingsFlow
            .flatMapLatest { appSettings ->
                val currentTableId = appSettings.currentCourseTableId
                val timeSlotsFlow = timeSlotRepository.getTimeSlotsByCourseTableId(currentTableId)
                val courseConfigFlow = appSettingsRepository.getCourseTableConfigFlow(currentTableId)

                combine(timeSlotsFlow, courseConfigFlow) { timeSlots, config ->
                    val classDuration = config?.defaultClassDuration ?: 45
                    val breakDuration = config?.defaultBreakDuration ?: 10

                    if (!isDataInitialized) {
                                                initialTimeSlots = timeSlots.map { it.copy(courseTableId = "") }.sortedBy { it.startTime }
                        initialClassDuration = classDuration
                        initialBreakDuration = breakDuration
                        isDataInitialized = true
                    }

                    TimeSlotUiState(
                        timeSlots = timeSlots,
                        defaultClassDuration = classDuration,
                        defaultBreakDuration = breakDuration,
                        isDataLoaded = true
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = TimeSlotUiState(emptyList(), 45, 10, false)
            )

    
    fun hasUnsavedChanges(
        currentTimeSlots: List<TimeSlot>,
        currentClassDuration: Int,
        currentBreakDuration: Int
    ): Boolean {
                if (!isDataInitialized) return false

                if (currentClassDuration != initialClassDuration) return true
        if (currentBreakDuration != initialBreakDuration) return true

                if (currentTimeSlots.size != initialTimeSlots.size) return true

        val normalizedCurrent = currentTimeSlots.map { it.copy(courseTableId = "") }.sortedBy { it.startTime }

        return normalizedCurrent != initialTimeSlots
    }

    
    private fun updateBackupPoint(timeSlots: List<TimeSlot>, classDuration: Int, breakDuration: Int) {
        initialTimeSlots = timeSlots.map { it.copy(courseTableId = "") }.sortedBy { it.startTime }
        initialClassDuration = classDuration
        initialBreakDuration = breakDuration
    }

    
    fun onSaveAllSettings(
        timeSlots: List<TimeSlot>,
        classDuration: Int,
        breakDuration: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentTableId = appSettingsRepository.getAppSettings().first().currentCourseTableId
            val allTables = courseTableRepository.getAllCourseTables().first()
            val allTableIds = allTables.map { it.id }

            val tableExists = allTableIds.contains(currentTableId)

            if (tableExists) {
                                val timeSlotsWithCorrectId = timeSlots.map { it.copy(courseTableId = currentTableId) }

                                timeSlotRepository.replaceAllForCourseTable(currentTableId, timeSlotsWithCorrectId)

                                val currentConfig = appSettingsRepository.getCourseConfigOnce(currentTableId)
                    ?: CourseTableConfig(courseTableId = currentTableId)

                val updatedConfig = currentConfig.copy(
                    defaultClassDuration = classDuration,
                    defaultBreakDuration = breakDuration
                )

                                appSettingsRepository.insertOrUpdateCourseConfig(updatedConfig)

                                updateBackupPoint(timeSlotsWithCorrectId, classDuration, breakDuration)

                onSuccess()
            }
        }
    }
}

data class TimeSlotUiState(
    val timeSlots: List<TimeSlot>,
    val defaultClassDuration: Int,
    val defaultBreakDuration: Int,
    val isDataLoaded: Boolean = false
)