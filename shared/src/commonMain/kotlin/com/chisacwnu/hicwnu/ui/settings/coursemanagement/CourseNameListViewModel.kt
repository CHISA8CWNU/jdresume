/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.coursemanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.CourseTableRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel


data class CourseNameCount(
    val name: String,
    val count: Int
)


@KoinViewModel
class CourseNameListViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val courseTableRepository: CourseTableRepository
) : ViewModel() {

        private val currentTableIdFlow = appSettingsRepository.getAppSettings()
        .map { it.currentCourseTableId }
        .distinctUntilChanged()

    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uniqueCourseNames: StateFlow<List<CourseNameCount>> = currentTableIdFlow
        .flatMapLatest { tableId ->
            if (tableId.isEmpty()) {
                flowOf(emptyList())
            } else {
                                courseTableRepository.getCoursesWithWeeksByTableId(tableId)
            }
        }
        .map { coursesWithWeeks ->
            coursesWithWeeks
                .groupBy { it.course.name }
                .map { (name, list) ->
                    CourseNameCount(
                        name = name,
                        count = list.size
                    )
                }
                .sortedBy { it.name }
        }
        .stateIn(
            scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    
    suspend fun deleteSelectedCourses(courseNames: List<String>) {
        if (courseNames.isEmpty()) return
        val appSettings = appSettingsRepository.getAppSettings().first()
        val tableId = appSettings.currentCourseTableId

        courseTableRepository.deleteCoursesByNames(tableId, courseNames)
    }
}