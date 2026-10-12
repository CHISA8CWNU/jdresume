/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.style

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.db.main.Course
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.data.model.DualColor
import com.chisacwnu.hicwnu.data.model.schedule_style.BorderTypeProto
import com.chisacwnu.hicwnu.data.model.schedule_style.ScheduleModeProto
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import com.chisacwnu.hicwnu.data.repository.StyleSettingsRepository
import com.chisacwnu.hicwnu.ui.schedule.MergedCourseBlock
import com.chisacwnu.hicwnu.ui.schedule.WeeklyScheduleUiState
import com.chisacwnu.hicwnu.ui.schedule.components.ScheduleGridStyleComposed
import com.chisacwnu.hicwnu.ui.schedule.components.ScheduleGridStyleComposed.Companion.toComposedStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class StyleSettingsViewModel(
    private val styleRepository: StyleSettingsRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val fileSystem: FileSystem,
    @Named("FilesDir") private val filesDir: Path
) : ViewModel() {

        val styleState: StateFlow<ScheduleGridStyleComposed?> = styleRepository.styleFlow
        .map { it.toComposedStyle() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val demoUiState: StateFlow<WeeklyScheduleUiState> = appSettingsRepository.getAppSettings()
        .flatMapLatest { settings ->
            val configFlow = settings.currentCourseTableId.let { tableId ->
                appSettingsRepository.getCourseTableConfigFlow(tableId)
            }

            combine(configFlow, styleRepository.styleFlow) { config, currentStyle ->
                val dummyTableId = Uuid.random().toString()
                val is24HourMode = currentStyle.scheduleMode == ScheduleModeProto.TIME_24H_MODE
                val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                WeeklyScheduleUiState(
                    style = currentStyle,
                    currentMergedCourses = createDemoCourses(dummyTableId, is24HourMode),
                    timeSlots = createDemoTimeSlots(dummyTableId),
                    showWeekends = config?.showWeekends ?: true,
                    totalWeeks = 20,
                    isSemesterSet = true,
                    semesterStartDate = todayDate,
                    firstDayOfWeek = 1,
                    currentWeekNumber = 1
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyScheduleUiState(showWeekends = true)
        )

    
    
    fun saveCroppedWallpaper(imageBytes: ByteArray) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val currentStyle = styleRepository.getStyleOnce()
            val currentPath = currentStyle.backgroundImagePath ?: ""

            if (currentPath.isNotEmpty()) {
                val oldPath = currentPath.toPath()
                if (fileSystem.exists(oldPath)) {
                    fileSystem.delete(oldPath)
                }
            }

            val newFileName = "wallpaper_${Uuid.random()}.jpg"
            val newFile = filesDir / newFileName

                        fileSystem.write(newFile) {
                write(imageBytes)
            }

            styleRepository.setBackgroundImagePath(newFile.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    
    fun removeWallpaper() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val currentStyle = styleRepository.getStyleOnce()
            val pathStr = currentStyle.backgroundImagePath ?: ""

            if (pathStr.isNotEmpty()) {
                val path = pathStr.toPath()
                if (fileSystem.exists(path)) {
                    fileSystem.delete(path)
                }
            }
            styleRepository.setBackgroundImagePath("")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    
    fun resetStyleSettings() = viewModelScope.launch {
        styleRepository.resetAllStyleSettingsExceptWallpaper()
    }

    
    fun resetEverything() = viewModelScope.launch(Dispatchers.IO) {
                removeWallpaper()
                styleRepository.resetAllStyleSettings()
    }

        fun updateSectionHeight(height: Float) = viewModelScope.launch { styleRepository.setSectionHeight(height) }
    fun updateTimeColumnWidth(width: Float) = viewModelScope.launch { styleRepository.setTimeColumnWidth(width) }

    
    fun updateDayHeaderHeight(height: Float) = viewModelScope.launch {
        styleRepository.setDayHeaderHeight(height)
    }

    fun updateCornerRadius(radius: Float) = viewModelScope.launch { styleRepository.setCourseBlockCornerRadius(radius) }
    fun updateOuterPadding(padding: Float) = viewModelScope.launch { styleRepository.setCourseBlockOuterPadding(padding) }

    
    fun updateInnerPadding(padding: Float) = viewModelScope.launch {
        styleRepository.setCourseBlockInnerPadding(padding)
    }

    fun updateAlpha(alpha: Float) = viewModelScope.launch { styleRepository.setCourseBlockAlpha(alpha) }

    
    
    fun updateHideGridLines(hide: Boolean) = viewModelScope.launch {
        styleRepository.setHideGridLines(hide)
    }

    
    fun updateHideSectionTime(hide: Boolean) = viewModelScope.launch {
        styleRepository.setHideSectionTime(hide)
    }

    
    fun updateHideDateUnderDay(hide: Boolean) = viewModelScope.launch {
        styleRepository.setHideDateUnderDay(hide)
    }

    
    fun updateShowStartTime(show: Boolean) = viewModelScope.launch {
        styleRepository.setShowStartTime(show)
    }

    
    fun updateScheduleMode(mode: ScheduleModeProto) = viewModelScope.launch {
        styleRepository.setScheduleMode(mode)
    }

    
    fun updateCourseBlockFontScale(scale: Float) = viewModelScope.launch {
        styleRepository.setCourseBlockFontScale(scale)
    }

    
    fun updateHideLocation(hide: Boolean) = viewModelScope.launch {
        styleRepository.setHideLocation(hide)
    }

    
    fun updateHideTeacher(hide: Boolean) = viewModelScope.launch {
        styleRepository.setHideTeacher(hide)
    }

    
    fun updateRemoveLocationAt(remove: Boolean) = viewModelScope.launch {
        styleRepository.setRemoveLocationAt(remove)
    }

    
    fun updateTextAlignCenterHorizontal(center: Boolean) = viewModelScope.launch {
        styleRepository.setTextAlignCenterHorizontal(center)
    }

    
    fun updateTextAlignCenterVertical(center: Boolean) = viewModelScope.launch {
        styleRepository.setTextAlignCenterVertical(center)
    }

    
    fun updateBorderType(type: BorderTypeProto) = viewModelScope.launch {
        styleRepository.setBorderType(type)
    }

    
    fun updatePageTextColor(color: Color?) = viewModelScope.launch {
        styleRepository.setPageTextColor(color)
    }

    
    fun updateCourseTextColor(color: Color?) = viewModelScope.launch {
        styleRepository.setCourseTextColor(color)
    }

    
    fun updatePrimaryColor(index: Int, color: Color, isDark: Boolean) = viewModelScope.launch {
                val currentStyle = styleRepository.getStyleOnce()
                val updatedMaps = currentStyle.courseColorMaps.toMutableList()

                if (index >= updatedMaps.size) {
            repeat(index - updatedMaps.size + 1) {
                updatedMaps.add(DualColor(light = Color.Gray, dark = Color.Gray))
            }
        }

                val oldPair = updatedMaps[index]
        updatedMaps[index] = if (isDark) {
            oldPair.copy(dark = color)
        } else {
            oldPair.copy(light = color)
        }

                styleRepository.setCourseColorMaps(updatedMaps)
    }

    private fun createDemoCourses(dummyTableId: String, is24HourMode: Boolean): List<MergedCourseBlock> {
        return if (is24HourMode) {
            listOf(
                                createBlock(
                    tableId = dummyTableId, day = 1, start = 0.0f, end = 1.75f, name = "深夜研讨",
                    startSection = null, endSection = null,
                    isCustomTime = true, customStartTime = "00:00", customEndTime = "00:45",
                    colorInt = 0
                ),
                createBlock(
                    tableId = dummyTableId, day = 2, start = 0.5f, end = 2.0f, name = "凌晨补录",
                    startSection = null, endSection = null,
                    isCustomTime = true, customStartTime = "01:00", customEndTime = "02:00",
                    colorInt = 1
                )
            )
        } else {
            listOf(
                                createBlock(
                    tableId = dummyTableId, day = 1, start = 0f, end = 2f, name = "普通课程展示",
                    startSection = 1, endSection = 2,
                    isCustomTime = false, customStartTime = null, customEndTime = null,
                    colorInt = 0
                ),
                                createBlock(
                    tableId = dummyTableId, day = 2, start = 0.618f, end = 2.5f, name = "精准渲染演示",
                    startSection = null, endSection = null,
                    isCustomTime = true, customStartTime = "09:15", customEndTime = "11:05",
                    colorInt = 1
                ),
                                createBlock(
                    tableId = dummyTableId, day = 3, start = 0f, end = 3f, name = "冲突课程 A",
                    startSection = 1, endSection = 3,
                    isCustomTime = false, customStartTime = null, customEndTime = null,
                    subColumn = 0f, totalColumns = 2f,
                    colorInt = 2
                ),
                                createBlock(
                    tableId = dummyTableId, day = 3, start = 1f, end = 3f, name = "冲突课程 B",
                    startSection = 2, endSection = 3,
                    isCustomTime = false, customStartTime = null, customEndTime = null,
                    subColumn = 1f, totalColumns = 2f,
                    isNonCurrentWeek = true,
                    colorInt = 3
                )
            )
        }
    }

    private fun createBlock(
        tableId: String,
        day: Int,
        start: Float,
        end: Float,
        name: String,
        startSection: Int?,
        endSection: Int?,
        isCustomTime: Boolean,
        customStartTime: String?,
        customEndTime: String?,
        subColumn: Float = 0f,
        totalColumns: Float = 1f,
        isNonCurrentWeek: Boolean = false,
        colorInt: Int
    ): MergedCourseBlock {
        val course = Course(
            id = Uuid.random().toString(),
            courseTableId = tableId,
            name = name,
            teacher = "老师",
            position = "地点",
            day = day,
            startSection = startSection,
            endSection = endSection,
            isCustomTime = isCustomTime,
            customStartTime = customStartTime,
            customEndTime = customEndTime,
            colorInt = colorInt,
            remark = ""
        )

        return MergedCourseBlock(
            day = day,
            startSection = start,
            endSection = end,
            courses = listOf(CourseWithWeeks(course, emptyList())),
            needsProportionalRendering = isCustomTime,
            isVisualDemoted = isNonCurrentWeek,
            nonActiveRanges = listOf(subColumn to totalColumns)
        )
    }

    private fun createDemoTimeSlots(dummyTableId: String): List<TimeSlot> = listOf(
        TimeSlot(1, "08:20", "09:05", dummyTableId),
        TimeSlot(2, "09:15", "10:00", dummyTableId),
        TimeSlot(3, "10:20", "11:05", dummyTableId),
        TimeSlot(4, "11:15", "12:00", dummyTableId),
        TimeSlot(5, "14:00", "14:45", dummyTableId),
        TimeSlot(6, "14:55", "15:40", dummyTableId),
        TimeSlot(7, "16:00", "16:45", dummyTableId),
        TimeSlot(8, "16:55", "17:40", dummyTableId)
    )
}