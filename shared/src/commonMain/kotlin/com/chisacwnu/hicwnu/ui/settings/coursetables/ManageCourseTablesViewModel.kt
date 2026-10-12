/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.coursetables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.db.main.CourseTable
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.CourseTableRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class ManageCourseTablesViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val courseTableRepository: CourseTableRepository
) : ViewModel() {

        val uiState: StateFlow<ManageCourseTablesUiState> = combine(
        courseTableRepository.getAllCourseTables(),
        appSettingsRepository.getAppSettings()
    ) { courseTables, appSettings ->
        ManageCourseTablesUiState(
            courseTables = courseTables,
            currentActiveTableId = appSettings.currentCourseTableId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManageCourseTablesUiState()
    )

    
    fun createNewCourseTable(newTableName: String) {
        viewModelScope.launch {
            courseTableRepository.createNewCourseTable(newTableName)
        }
    }

    
    fun updateCourseTable(updatedCourseTable: CourseTable) {
        viewModelScope.launch {
            courseTableRepository.updateCourseTable(updatedCourseTable)
        }
    }

    
    fun switchCourseTable(tableId: String) {
        viewModelScope.launch {
            val currentSettings = appSettingsRepository.getAppSettings().first()
            val newSettings = currentSettings.copy(currentCourseTableId = tableId)
            appSettingsRepository.insertOrUpdateAppSettings(newSettings)
        }
    }

    
    fun deleteCourseTable(courseTable: CourseTable) {
        viewModelScope.launch {
            val success = courseTableRepository.deleteCourseTable(courseTable)
            if (success) {
                                if (courseTable.id == uiState.value.currentActiveTableId) {
                    val remainingTables = courseTableRepository.getAllCourseTables().first()
                    if (remainingTables.isNotEmpty()) {
                        switchCourseTable(remainingTables.first().id)
                    }
                }
            }
        }
    }
}


data class ManageCourseTablesUiState(
    val courseTables: List<CourseTable> = emptyList(),
    val currentActiveTableId: String? = null
)