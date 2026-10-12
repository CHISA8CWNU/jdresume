/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schedule.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.chisacwnu.hicwnu.data.db.main.Course
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.ui.schedule.MergedCourseBlock
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.banner_dinner
import hicwnu.shared.generated.resources.banner_early_study
import hicwnu.shared.generated.resources.banner_evening_study
import hicwnu.shared.generated.resources.banner_lunch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


val EARLY_STUDY_DUMMY = CourseWithWeeks(
    course = Course(
        id = "__early_study__",
        courseTableId = "",
        name = "早自习",
        teacher = "",
        position = "",
        day = 0,
        startSection = 0,
        endSection = 0,
        isCustomTime = false,
        customStartTime = null,
        customEndTime = null,
        colorInt = 0,
        remark = null
    ),
    weeks = emptyList()
)

val EVENING_STUDY_DUMMY = CourseWithWeeks(
    course = Course(
        id = "__evening_study__",
        courseTableId = "",
        name = "晚自习",
        teacher = "",
        position = "",
        day = 0,
        startSection = 0,
        endSection = 0,
        isCustomTime = false,
        customStartTime = null,
        customEndTime = null,
        colorInt = 0,
        remark = null
    ),
    weeks = emptyList()
)

interface ISingleSchedulable {
    val columnIndex: Int
    val subColumnIndex: Int
    val subColumnCount: Int
    val startSection: Float
    val endSection: Float
    val courseWrapper: CourseWithWeeks
    val parentBlock: MergedCourseBlock
    val columnSpan: Int get() = 1
}


data class CourseMoveIntent(
    val parentBlock: MergedCourseBlock,         val initialDay: Int,                       val initialStartSection: Float,            val duration: Float                    )


@Immutable
data class ScheduleGridViewState(
    val dates: List<String>,
    val currentYear: String,
    val currentWeek: String? = null,
    val timeSlots: List<TimeSlot>,
    val mergedCourses: List<MergedCourseBlock>,
    val showWeekends: Boolean,
    val todayIndex: Int,
    val firstDayOfWeek: Int,
    val currentSectionIndex: Int = -1
)


interface ScheduleGridActions {
    fun onCourseBlockClicked(block: MergedCourseBlock)
    fun onGridCellClicked(day: Int, section: Int)
    fun onTimeSlotClicked()
    fun onHoldStateChanged(isHolding: Boolean) {}
    fun onCourseMovedWithinGrid(block: MergedCourseBlock, newDay: Int, newStartSection: Float, newEndSection: Float) {}
    fun onCourseTimeAdjusted(block: MergedCourseBlock, newStart: Float, newEnd: Float) {}
    fun onInitiateFloatingMode(block: MergedCourseBlock) {}
}


@Stable
class ScheduleGridState(
    val gridScrollState: ScrollState
) {
    var expandedItem by mutableStateOf<ISingleSchedulable?>(null)

    var activeMoveIntent by mutableStateOf<CourseMoveIntent?>(null)
    var bodyDragOffsetX by mutableStateOf(0f)
    var bodyDragOffsetY by mutableStateOf(0f)

    var isTopHandleDragging by mutableStateOf(false)
    var isBottomHandleDragging by mutableStateOf(false)

    var topHandleDragOffsetY by mutableStateOf(0f)
    var bottomHandleDragOffsetY by mutableStateOf(0f)

    var gridWidthPx by mutableStateOf(0f)
    var viewportHeightPx by mutableStateOf(0f)

    val isEditingActive: Boolean
        get() = expandedItem != null && (activeMoveIntent != null || isTopHandleDragging || isBottomHandleDragging)

    fun resetAllStates() {
        expandedItem = null
        activeMoveIntent = null
        isTopHandleDragging = false
        isBottomHandleDragging = false
        topHandleDragOffsetY = 0f
        bottomHandleDragOffsetY = 0f
        bodyDragOffsetX = 0f
        bodyDragOffsetY = 0f
    }
}


@Composable
fun rememberScheduleGridState(
    gridScrollState: ScrollState = rememberScrollState()
): ScheduleGridState {
    return remember(gridScrollState) {
        ScheduleGridState(gridScrollState)
    }
}


@Composable
fun DayHeader(
    style: ScheduleGridStyleComposed,
    displayDays: List<String>,
    dates: List<String>,
    currentYear: String,
    currentWeek: String?,
    todayIndex: Int,
    lineColor: Color,
    textColor: Color,
    subTextColor: Color,
    strokeWidthPx: Float
) {
    Box(Modifier.fillMaxWidth().height(style.dayHeaderHeight)) {
        val shouldShowDate = !style.hideDateUnderDay && style.dayHeaderHeight >= 42.dp

        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(style.timeColumnWidth)
                    .fillMaxHeight()
                    .drawBehind {
                        if (!style.hideGridLines) {
                            drawLine(lineColor, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidthPx)
                            drawLine(lineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidthPx)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentYear,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = subTextColor,
                        style = TextStyle.Default
                    )

                    if (!currentWeek.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = currentWeek,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = subTextColor,
                            style = TextStyle.Default
                        )
                    }
                }
            }

            val displayDaysCount = displayDays.size

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .drawBehind {
                        if (!style.hideGridLines) {
                            val cellWidth = size.width / displayDaysCount
                            for (i in 1..displayDaysCount) {
                                val x = i * cellWidth
                                drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = strokeWidthPx)
                            }
                            drawLine(lineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidthPx)
                        }
                    }
            ) {
                displayDays.forEachIndexed { index, day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (index == todayIndex) MaterialTheme.colorScheme.primaryContainer.copy(0.4f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                                style = TextStyle(
                                    lineHeight = 16.sp
                                )
                            )

                            if (shouldShowDate && dates.size > index) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dates[index],
                                    fontSize = 10.sp,
                                    color = subTextColor,
                                    maxLines = 1,
                                    style = TextStyle(
                                        lineHeight = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TimeColumnEntry(
    val slot: TimeSlot? = null,
    val separatorLabel: String? = null
)

fun buildTimeColumnRows(
    timeSlots: List<TimeSlot>,
    lunchLabel: String,
    dinnerLabel: String
): List<TimeColumnEntry> {
    val rows = mutableListOf<TimeColumnEntry>()
    for (slot in timeSlots) {
        rows.add(TimeColumnEntry(slot = slot))
        if (slot.number == 5) rows.add(TimeColumnEntry(separatorLabel = lunchLabel))
        if (slot.number == 9) rows.add(TimeColumnEntry(separatorLabel = dinnerLabel))
    }
    return rows
}

@Composable
fun TimeColumn(
    style: ScheduleGridStyleComposed,
    timeSlots: List<TimeColumnEntry>,
    maxGridSections: Int,
    is24HourMode: Boolean,
    onTimeSlotClicked: () -> Unit,
    modifier: Modifier,
    lineColor: Color,
    currentSectionIndex: Int = -1,
    textColor: Color,
    subTextColor: Color,
    strokeWidthPx: Float,
    activeDragHour: Int? = null,
    activeDragMinuteStr: String? = null
) {
    val currentHour = remember {
        try {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        } catch (e: Exception) {
            -1
        }
    }

    Column(modifier.width(style.timeColumnWidth)) {
        for (index in 0 until maxGridSections) {
            val entry = timeSlots.getOrNull(index)
            val isSeparator = entry?.separatorLabel != null
            val isCurrentHourActive = if (is24HourMode) {
                currentHour == index && currentSectionIndex != -1
            } else {
                index + 1 == currentSectionIndex
            }

            val cellBackgroundColor = when {
                isCurrentHourActive -> MaterialTheme.colorScheme.primaryContainer.copy(0.4f)
                isSeparator -> MaterialTheme.colorScheme.tertiaryContainer.copy(0.5f)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(style.sectionHeight)
                    .clickable { onTimeSlotClicked() }
                    .background(cellBackgroundColor)
                    .drawBehind {
                        if (!style.hideGridLines) {
                            drawLine(lineColor, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidthPx)
                            if (!is24HourMode) {
                                drawLine(lineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidthPx)
                            }
                        }
                    },
                contentAlignment = if (is24HourMode) Alignment.TopCenter else Alignment.Center
            ) {
                val h = style.sectionHeight
                if (is24HourMode && activeDragMinuteStr != null && index == activeDragHour) {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ":$activeDragMinuteStr",
                            fontSize = if (h < 32.dp) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (is24HourMode) Arrangement.Top else Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .then(if (is24HourMode) Modifier.offset(y = (-7).dp) else Modifier)
                ) {
                    if (is24HourMode) {
                        val formatHourStr = "${index.toString().padStart(2, '0')}:00"
                        Text(
                            text = formatHourStr,
                            fontSize = if (h < 32.dp) 11.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCurrentHourActive) MaterialTheme.colorScheme.primary else textColor
                        )
                    } else {
                        val entry = timeSlots.getOrNull(index)
                        if (entry?.separatorLabel != null) {
                            Text(
                                text = entry.separatorLabel,
                                fontSize = if (h < 32.dp) 11.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (entry?.slot != null) {
                            val slot = entry.slot
                            Text(
                                text = slot.alias ?: slot.number.toString(),
                                fontSize = if (h < 32.dp) 11.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!style.hideSectionTime) {
                                when {
                                    h >= 52.dp -> {
                                        Spacer(Modifier.height(2.dp))
                                        TimeText(slot.startTime, subTextColor)
                                        TimeText(slot.endTime, subTextColor)
                                    }
                                    h >= 38.dp -> {
                                        Text(text = "${slot.startTime}-${slot.endTime}", fontSize = 8.sp, color = subTextColor, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BoxScope.CourseEditHandles(
    onDragStart: (isTop: Boolean) -> Unit,
    onDragging: (deltaY: Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val handleBoxSize = 32.dp

    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = (-16).dp, y = (-16).dp)
            .size(handleBoxSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart(true) },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragging(dragAmount.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        EditHandleDot()
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 16.dp, y = 16.dp)
            .size(handleBoxSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart(false) },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragging(dragAmount.y)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        EditHandleDot()
    }
}

@Composable
private fun EditHandleDot() {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(Color(0xFF2196F3), CircleShape)
    )
}

@Composable
fun TimeText(text: String, color: Color) {
    Text(text = text, fontSize = 10.sp, color = color, style = TextStyle(lineHeight = 1.em))
}

@Composable
fun MealBreakBanner(style: ScheduleGridStyleComposed, label: String) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.tertiaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.onTertiaryContainer
        )
    }
}

@Composable
fun EveningStudyBanner(style: ScheduleGridStyleComposed) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.secondaryContainer.copy(alpha = 0.55f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.banner_evening_study),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.onSecondaryContainer
        )
    }
}

@Composable
fun EarlyStudyBanner(style: ScheduleGridStyleComposed, range: String) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.primary.copy(alpha = 0.14f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(Res.string.banner_early_study),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
            if (range.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = range,
                    fontSize = 10.sp,
                    color = scheme.primary.copy(alpha = 0.85f)
                )
            }
        }
    }
}


fun calculateSingleSchedulables(
    mergedCourses: List<MergedCourseBlock>,
    firstDayOfWeek: Int,
    showWeekends: Boolean
): List<ISingleSchedulable> {
    val list = mutableListOf<ISingleSchedulable>()
    mergedCourses.forEach { block ->
        if (block.isEarlyStudy) {

            val monThuIndices = (1..4).mapNotNull { d ->
                val idx = mapDayToDisplayIndex(d, firstDayOfWeek, showWeekends)
                if (idx < 0) null else idx
            }
            val spanStart = monThuIndices.minOrNull() ?: 0
            val spanCount = (monThuIndices.maxOrNull() ?: 0) - spanStart + 1
            list.add(object : ISingleSchedulable {
                override val columnIndex = spanStart
                override val subColumnIndex = 0
                override val subColumnCount = 1
                override val columnSpan = spanCount
                override val startSection = block.startSection
                override val endSection = block.endSection
                override val courseWrapper = block.courses.firstOrNull() ?: EARLY_STUDY_DUMMY
                override val parentBlock = block
            })
            return@forEach
        }
        if (block.isMealBreak) {

            val colCount = if (showWeekends) 7 else 5
            list.add(object : ISingleSchedulable {
                override val columnIndex = 0
                override val subColumnIndex = 0
                override val subColumnCount = 1
                override val columnSpan = colCount
                override val startSection = block.startSection
                override val endSection = block.endSection
                override val courseWrapper = EARLY_STUDY_DUMMY
                override val parentBlock = block
            })
            return@forEach
        }
        if (block.isEveningStudy) {

            val displayIdx = mapDayToDisplayIndex(block.day, firstDayOfWeek, showWeekends)
            if (displayIdx != -1) {
                list.add(object : ISingleSchedulable {
                    override val columnIndex = displayIdx
                    override val subColumnIndex = 0
                    override val subColumnCount = 1
                    override val startSection = block.startSection
                    override val endSection = block.endSection
                    override val courseWrapper = block.courses.firstOrNull() ?: EVENING_STUDY_DUMMY
                    override val parentBlock = block
                })
            }
            return@forEach
        }
        val displayIdx = mapDayToDisplayIndex(block.day, firstDayOfWeek, showWeekends)
        if (displayIdx != -1) {
            val layoutInfo = block.nonActiveRanges.firstOrNull() ?: (0f to 1f)
            block.courses.firstOrNull()?.let { course ->
                list.add(object : ISingleSchedulable {
                    override val columnIndex = displayIdx
                    override val subColumnIndex = layoutInfo.first.toInt()
                    override val subColumnCount = layoutInfo.second.toInt()
                    override val startSection = block.startSection
                    override val endSection = block.endSection
                    override val courseWrapper = course
                    override val parentBlock = block.copy(courses = listOf(course))
                })
            }
        }
    }
    return list
}

fun rearrangeDays(originalDays: List<String>, firstDayOfWeek: Int): List<String> {
    val startIndex = (firstDayOfWeek - 1).coerceIn(0, 6)
    return originalDays.subList(startIndex, originalDays.size) + originalDays.subList(0, startIndex)
}

fun sectionToGridRow(scale: Float, hasEarlyStudy: Boolean, isEnd: Boolean = false): Float {
    val earlyOffset = if (hasEarlyStudy) 1f else 0f
    val base = scale - 1f
    val lunchOffset = if (isEnd) {
        if (scale > 6f) 1f else 0f
    } else {
        if (scale >= 6f) 1f else 0f
    }
    val dinnerOffset = if (isEnd) {
        if (scale > 10f) 1f else 0f
    } else {
        if (scale >= 10f) 1f else 0f
    }
    return earlyOffset + base + lunchOffset + dinnerOffset
}

fun mapDayToDisplayIndex(courseDay: Int, firstDayOfWeek: Int, showWeekends: Boolean): Int {
    val idx = (courseDay - firstDayOfWeek + 7) % 7
    return if (idx >= if (showWeekends) 7 else 5) -1 else idx
}

fun mapDisplayIndexToDay(idx: Int, firstDayOfWeek: Int): Int = (firstDayOfWeek - 1 + idx) % 7 + 1