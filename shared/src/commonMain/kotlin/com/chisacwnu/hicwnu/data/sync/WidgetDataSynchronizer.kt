/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.sync

import com.chisacwnu.hicwnu.data.db.main.CourseTableConfig
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.data.db.widget.WidgetAppSettings
import com.chisacwnu.hicwnu.data.db.widget.WidgetCourse
import com.chisacwnu.hicwnu.data.model.AppSettingsModel
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.CourseTableRepository
import com.chisacwnu.hicwnu.data.repository.TimeSlotRepository
import com.chisacwnu.hicwnu.data.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds


@Single(createdAtStart = true)
class WidgetDataSynchronizer(
    private val appSettingsRepository: AppSettingsRepository,
    private val courseTableRepository: CourseTableRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val widgetRepository: WidgetRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val widgetSyncDays = 7
    private val isStarted = MutableStateFlow(false)

        private val _syncCompletedChannel = Channel<Unit>(Channel.CONFLATED)

    
    val syncCompletedFlow: Flow<Unit> = _syncCompletedChannel.receiveAsFlow()

    
    @OptIn(ExperimentalCoroutinesApi::class)
    private val syncFlow: Flow<Unit> = appSettingsRepository.getAppSettings()
        .flatMapLatest { appSettings ->
            val tableId = appSettings.currentCourseTableId

            if (tableId.isNotEmpty()) {
                val coursesFlow = courseTableRepository.getCoursesWithWeeksByTableId(tableId)
                val timeSlotsFlow = timeSlotRepository.getTimeSlotsByCourseTableId(tableId)
                val configFlow = appSettingsRepository.getCourseTableConfigFlow(tableId)

                                combine(coursesFlow, timeSlotsFlow, configFlow) { courses, timeSlots, config ->
                    Quadruple(appSettings, courses, timeSlots, config)
                }
            } else {
                flowOf(Quadruple(appSettings, emptyList(), emptyList(), null))
            }
        }
        .map { (appSettings, coursesWithWeeks, timeSlots, config) ->
            if (config != null) {
                performSync(appSettings, config, coursesWithWeeks, timeSlots)
            } else {
                                widgetRepository.deleteAll()
                widgetRepository.insertOrUpdateAppSettings(WidgetAppSettings(id = 1, semesterStartDate = null))
            }
        }

    init {
                startSync()
    }

    
    @OptIn(FlowPreview::class)
    fun startSync() {
                if (isStarted.value) return
        isStarted.value = true

                syncFlow
            .debounce(500.milliseconds)
            .onEach {
                _syncCompletedChannel.trySend(Unit)
            }
            .launchIn(scope)

                appSettingsRepository.getAppSettings()
            .map { settings ->
                Triple(
                    settings.reminderEnabled to settings.remindBeforeMinutes,
                    settings.autoModeEnabled to settings.autoControlMode,
                    settings.compatWearableSync
                )
            }
            .distinctUntilChanged()
            .onEach {
                _syncCompletedChannel.trySend(Unit)
            }
            .launchIn(scope)
    }

    
    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    
    suspend fun syncNow() {
        val appSettings = appSettingsRepository.getAppSettings().first()
        val tableId = appSettings.currentCourseTableId

        val coursesWithWeeks = if (tableId.isNotEmpty()) courseTableRepository.getCoursesWithWeeksByTableId(tableId).first() else emptyList()
        val timeSlots = if (tableId.isNotEmpty()) timeSlotRepository.getTimeSlotsByCourseTableId(tableId).first() else emptyList()
        val courseConfig = if (tableId.isNotEmpty()) appSettingsRepository.getCourseConfigOnce(tableId) else null

        if (courseConfig != null) {
            performSync(appSettings, courseConfig, coursesWithWeeks, timeSlots)
        } else {
            widgetRepository.deleteAll()
            widgetRepository.insertOrUpdateAppSettings(WidgetAppSettings(id = 1, semesterStartDate = null))
        }
                _syncCompletedChannel.trySend(Unit)
    }

    
    private suspend fun performSync(
        appSettings: AppSettingsModel,
        courseConfig: CourseTableConfig,
        coursesWithWeeks: List<CourseWithWeeks>,
        timeSlots: List<TimeSlot>
    ) = withContext(Dispatchers.IO) {
        val semesterStartDateString = courseConfig.semesterStartDate ?: run {
            widgetRepository.deleteAll()
            widgetRepository.insertOrUpdateAppSettings(WidgetAppSettings(id = 1, semesterStartDate = null))
            return@withContext
        }
        val semesterTotalWeeks = courseConfig.semesterTotalWeeks
        val firstDayOfWeekInt = courseConfig.firstDayOfWeek

        if (semesterTotalWeeks <= 0) {
            widgetRepository.deleteAll()
            widgetRepository.insertOrUpdateAppSettings(WidgetAppSettings(id = 1, semesterStartDate = null))
            return@withContext
        }

                val widgetSettings = WidgetAppSettings(
            id = 1,
            semesterStartDate = semesterStartDateString,
            semesterTotalWeeks = semesterTotalWeeks,
            firstDayOfWeek = firstDayOfWeekInt
        )
        widgetRepository.insertOrUpdateAppSettings(widgetSettings)

        val skippedDates = appSettings.skippedDates
        val timeSlotMap = timeSlots.associateBy { it.number }
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val semesterStartDate: LocalDate = try {
            LocalDate.parse(semesterStartDateString)
        } catch (e: Exception) {
            widgetRepository.deleteAll()
            widgetRepository.insertOrUpdateAppSettings(WidgetAppSettings(id = 1, semesterStartDate = null))
            return@withContext
        }

                val alignedSemesterStartDate = getStartDayOfWeek(semesterStartDate, firstDayOfWeekInt)

        val widgetCourses = mutableListOf<WidgetCourse>()
                val startSyncDate = if (today < alignedSemesterStartDate) {
            alignedSemesterStartDate
        } else {
            today
        }

                for (i in 0 until widgetSyncDays) {
            val date = startSyncDate.plus(i, DateTimeUnit.DAY)
            val dateString = date.toString()

            val alignedDate = getStartDayOfWeek(date, firstDayOfWeekInt)

                        val diffDays = alignedSemesterStartDate.daysUntil(alignedDate)
            val diffWeeks = diffDays / 7
            val weekNumber = diffWeeks + 1

            val dayOfWeek = date.dayOfWeek.isoDayNumber

                        if (weekNumber !in 1..semesterTotalWeeks) {
                continue
            }

                        for (courseWithWeeks in coursesWithWeeks) {
                if (courseWithWeeks.weeks.any { it.weekNumber == weekNumber } && courseWithWeeks.course.day == dayOfWeek) {
                    val course = courseWithWeeks.course

                    val startTime: String
                    val endTime: String

                                        if (course.isCustomTime) {
                        startTime = course.customStartTime ?: ""
                        endTime = course.customEndTime ?: ""
                    } else {
                        startTime = timeSlotMap[course.startSection]?.startTime ?: ""
                        endTime = timeSlotMap[course.endSection]?.endTime ?: ""
                    }

                                        val isSkipped = skippedDates.contains(dateString)

                    val widgetCourse = WidgetCourse(
                        id = "${course.id}-$dateString",
                        name = course.name,
                        teacher = course.teacher,
                        position = course.position,
                        startTime = startTime,
                        endTime = endTime,
                        isSkipped = isSkipped,
                        date = dateString,
                        colorInt = course.colorInt
                    )
                    widgetCourses.add(widgetCourse)
                }
            }
        }

                widgetRepository.deleteAll()
        if (widgetCourses.isNotEmpty()) {
            widgetRepository.insertAll(widgetCourses)
        }
    }

    
    private fun getStartDayOfWeek(date: LocalDate, firstDayOfWeekInt: Int): LocalDate {
        val targetFirstDay = DayOfWeek(firstDayOfWeekInt)
        var current = date
        while (current.dayOfWeek != targetFirstDay) {
            current = current.minus(1, DateTimeUnit.DAY)
        }
        return current
    }
}