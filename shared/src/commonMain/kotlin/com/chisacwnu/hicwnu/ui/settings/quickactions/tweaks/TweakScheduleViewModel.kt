/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.quickactions.tweaks

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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.until
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.KoinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.error_tweak_failed
import hicwnu.shared.generated.resources.error_tweak_no_table_or_semester
import hicwnu.shared.generated.resources.error_tweak_same_day
import hicwnu.shared.generated.resources.toast_tweak_success
import kotlin.time.Clock


data class UiTextRes(
    val resource: StringResource,
    val args: List<Any> = emptyList()
)


private fun todayLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())


data class TweakScheduleUiState(
        val allCourseTables: List<CourseTable> = emptyList(),
    val selectedCourseTable: CourseTable? = null,
    val fromDate: LocalDate = todayLocalDate(),
    val toDate: LocalDate = todayLocalDate(),
    val fromCourses: List<CourseWithWeeks> = emptyList(),
    val toCourses: List<CourseWithWeeks> = emptyList(),
    val tweakMode: CourseTableRepository.TweakMode = CourseTableRepository.TweakMode.MERGE,

        val isSemesterSet: Boolean = false,
    val semesterStartDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: UiTextRes? = null,
    val successMessage: UiTextRes? = null
)


@KoinViewModel
class TweakScheduleViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val courseTableRepository: CourseTableRepository
) : ViewModel() {

        private val _uiState = MutableStateFlow(TweakScheduleUiState())
    val uiState: StateFlow<TweakScheduleUiState> = _uiState.asStateFlow()

        private val _fromDate = MutableStateFlow(todayLocalDate())
    private val _toDate = MutableStateFlow(todayLocalDate())
    private val _selectedCourseTableByUser = MutableStateFlow<CourseTable?>(null)

    init {
        viewModelScope.launch {
            refreshUiState(isInitialLoad = true)
        }
    }

    
    private suspend fun refreshUiState(isInitialLoad: Boolean = false) {
        val settings = appSettingsRepository.getAppSettings().first()
        val allTables = courseTableRepository.getAllCourseTables().first()

        val selectedTable = if (isInitialLoad) {
            val defaultSelectedTable = allTables.find { it.id == settings.currentCourseTableId }
            _selectedCourseTableByUser.value = defaultSelectedTable
            defaultSelectedTable
        } else {
            _selectedCourseTableByUser.value
        }

        val currentFromDate = _fromDate.value
        val currentToDate = _toDate.value

        val currentTableId = selectedTable?.id
        val courseConfig = if (currentTableId != null) {
            appSettingsRepository.getCourseConfigOnce(currentTableId)
        } else {
            null
        }

        val semesterStartDateString = courseConfig?.semesterStartDate
        val semesterStartDate: LocalDate? = try {
            semesterStartDateString?.let { LocalDate.parse(it) }
        } catch (_: Exception) {
            null
        }
        val isSemesterSet = semesterStartDate != null

        var fromCourses = emptyList<CourseWithWeeks>()
        var toCourses = emptyList<CourseWithWeeks>()

        if (isSemesterSet && selectedTable != null) {
            val fromWeekNumber = semesterStartDate.until(currentFromDate, DateTimeUnit.WEEK).toInt() + 1
            val fromDay = currentFromDate.dayOfWeek.ordinal + 1
            val toWeekNumber = semesterStartDate.until(currentToDate, DateTimeUnit.WEEK).toInt() + 1
            val toDay = currentToDate.dayOfWeek.ordinal + 1

            fromCourses = courseTableRepository.getCoursesForDay(selectedTable.id, fromWeekNumber, fromDay).first()
            toCourses = courseTableRepository.getCoursesForDay(selectedTable.id, toWeekNumber, toDay).first()
        }

        _uiState.update {
            it.copy(
                allCourseTables = allTables,
                isSemesterSet = isSemesterSet,
                selectedCourseTable = selectedTable,
                fromDate = currentFromDate,
                toDate = currentToDate,
                fromCourses = fromCourses,
                toCourses = toCourses,
                semesterStartDate = semesterStartDate,
                isLoading = false
            )
        }
    }

        fun onTweakModeChanged(mode: CourseTableRepository.TweakMode) {
        _uiState.update { it.copy(tweakMode = mode) }
    }

    fun onCourseTableSelected(courseTable: CourseTable) {
        _selectedCourseTableByUser.value = courseTable
        viewModelScope.launch { refreshUiState() }
    }

    fun onFromDateSelected(date: LocalDate) {
        _fromDate.value = date
        viewModelScope.launch { refreshUiState() }
    }

    fun onToDateSelected(date: LocalDate) {
        _toDate.value = date
        viewModelScope.launch { refreshUiState() }
    }

    
    fun moveCourses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val state = _uiState.value

            if (state.selectedCourseTable == null || state.semesterStartDate == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiTextRes(Res.string.error_tweak_no_table_or_semester)
                    )
                }
                return@launch
            }

            if (state.fromDate == state.toDate) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiTextRes(Res.string.error_tweak_same_day)
                    )
                }
                return@launch
            }

            try {
                val semesterStartDate = state.semesterStartDate
                val fromWeek = semesterStartDate.until(state.fromDate, DateTimeUnit.WEEK).toInt() + 1
                val fromDay = state.fromDate.dayOfWeek.ordinal + 1
                val toWeek = semesterStartDate.until(state.toDate, DateTimeUnit.WEEK).toInt() + 1
                val toDay = state.toDate.dayOfWeek.ordinal + 1

                courseTableRepository.tweakCoursesOnDate(
                    mode = state.tweakMode,                     courseTableId = state.selectedCourseTable.id,
                    fromWeek = fromWeek,
                    fromDay = fromDay,
                    toWeek = toWeek,
                    toDay = toDay
                )

                                refreshUiState()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = UiTextRes(Res.string.toast_tweak_success)
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = UiTextRes(Res.string.error_tweak_failed, listOf(e.message ?: ""))
                    )
                }
            }
        }
    }

    fun resetMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}