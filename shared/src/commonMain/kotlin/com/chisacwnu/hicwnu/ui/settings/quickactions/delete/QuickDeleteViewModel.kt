/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.quickactions.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.db.main.CourseTable
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.CourseTableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.KoinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.error_load_failed
import hicwnu.shared.generated.resources.error_op_failed
import hicwnu.shared.generated.resources.quick_delete_success


data class UiTextRes(
    val resource: StringResource,
    val args: List<Any> = emptyList()
)


data class AffectedCourseItem(
    val courseWithWeeks: CourseWithWeeks,
    val targetWeek: Int
)

data class QuickDeleteUiState(
    val allCourseTables: List<CourseTable> = emptyList(),
    val selectedCourseTable: CourseTable? = null,

        val selectedWeeks: Set<Int> = emptySet(),
    val selectedDays: Set<Int> = emptySet(),

        val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

        val affectedCourses: List<AffectedCourseItem> = emptyList(),
    val semesterStartDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: UiTextRes? = null,
    val successMessage: UiTextRes? = null
)

@KoinViewModel
class QuickDeleteViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val courseTableRepository: CourseTableRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickDeleteUiState())
    val uiState: StateFlow<QuickDeleteUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                                val settings = appSettingsRepository.getAppSettings().first()
                val allTables = courseTableRepository.getAllCourseTables().first()
                val selectedTable = allTables.find { it.id == settings.currentCourseTableId } ?: allTables.firstOrNull()

                                val courseConfig = selectedTable?.let { appSettingsRepository.getCourseConfigOnce(it.id) }
                val semesterStartDate = try {
                    courseConfig?.semesterStartDate?.let { LocalDate.parse(it) }
                } catch (_: Exception) { null }

                _uiState.update { it.copy(
                    allCourseTables = allTables,
                    selectedCourseTable = selectedTable,
                    semesterStartDate = semesterStartDate
                ) }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = UiTextRes(Res.string.error_load_failed)) }
            }
        }
    }

    
    fun toggleWeek(week: Int) {
        _uiState.update { s ->
            val new = if (s.selectedWeeks.contains(week)) s.selectedWeeks - week else s.selectedWeeks + week
            s.copy(selectedWeeks = new)
        }
        refreshPreview()
    }

    fun toggleDay(day: Int) {
        _uiState.update { s ->
            val new = if (s.selectedDays.contains(day)) s.selectedDays - day else s.selectedDays + day
            s.copy(selectedDays = new)
        }
        refreshPreview()
    }

    fun selectAllDays() {
        _uiState.update { it.copy(selectedDays = (1..7).toSet()) }
        refreshPreview()
    }

    fun clearAllDays() {
        _uiState.update { it.copy(selectedDays = emptySet()) }
        refreshPreview()
    }

    fun clearWeeksAndDays() {
        _uiState.update { it.copy(selectedWeeks = emptySet(), selectedDays = emptySet()) }
        refreshPreview()
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        refreshPreview()
    }

    fun clearDateRange() {
        _uiState.update { it.copy(startDate = null, endDate = null) }
        refreshPreview()
    }

    
    private fun refreshPreview() {
        viewModelScope.launch {
            val state = _uiState.value
            val tableId = state.selectedCourseTable?.id ?: return@launch
            val semesterStart = state.semesterStartDate ?: return@launch

            val noWeekDaySelection = state.selectedWeeks.isEmpty() || state.selectedDays.isEmpty()
            val noDateRangeSelection = state.startDate == null || state.endDate == null

            if (noWeekDaySelection && noDateRangeSelection) {
                _uiState.update { it.copy(affectedCourses = emptyList()) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            val queryPairs = mutableSetOf<Pair<Int, Int>>()

                        if (state.selectedWeeks.isNotEmpty() && state.selectedDays.isNotEmpty()) {
                state.selectedWeeks.forEach { w ->
                    state.selectedDays.forEach { d -> queryPairs.add(w to d) }
                }
            }

                        state.startDate?.let { start ->
                state.endDate?.let { end ->
                    var curr: LocalDate = start
                    while (curr <= end) {
                        val daysBetween = semesterStart.until(curr, DateTimeUnit.DAY)
                        val w = (daysBetween / 7).toInt() + 1
                        val d = curr.dayOfWeek.ordinal + 1
                        queryPairs.add(w to d)
                        curr = curr.plus(DatePeriod(days = 1))
                    }
                }
            }

                        val flatList = mutableListOf<AffectedCourseItem>()
            queryPairs.forEach { (w, d) ->
                val coursesForDay = courseTableRepository.getCoursesForDay(tableId, w, d).first()
                coursesForDay.forEach { courseWithWeeks ->
                    flatList.add(AffectedCourseItem(courseWithWeeks, w))
                }
            }

                        val sortedList = flatList.sortedWith(
                compareBy({ it.targetWeek }, { it.courseWithWeeks.course.day }, { it.courseWithWeeks.course.startSection })
            )

            _uiState.update { it.copy(affectedCourses = sortedList, isLoading = false) }
        }
    }

    
    fun executeDelete() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentAffected = state.affectedCourses
            val tableId = state.selectedCourseTable?.id ?: return@launch

            if (currentAffected.isEmpty()) return@launch

            try {
                _uiState.update { it.copy(isLoading = true) }

                                val pairs = currentAffected.map { it.targetWeek to it.courseWithWeeks.course.day }.distinct()

                                courseTableRepository.deleteCoursesOnDates(tableId, pairs)

                _uiState.update {
                    it.copy(
                        successMessage = UiTextRes(Res.string.quick_delete_success),
                        affectedCourses = emptyList(),
                        isLoading = false,
                                                selectedWeeks = emptySet(),
                        selectedDays = emptySet(),
                        startDate = null,
                        endDate = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = UiTextRes(Res.string.error_op_failed, listOf(e.message ?: "")),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun resetMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}