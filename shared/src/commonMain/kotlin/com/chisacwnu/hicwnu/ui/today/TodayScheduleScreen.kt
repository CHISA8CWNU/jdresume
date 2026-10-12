/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.today
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import hicwnu.shared.generated.resources.dorm_water_active
import hicwnu.shared.generated.resources.dorm_hot_water
import hicwnu.shared.generated.resources.dorm_lights_out_tonight
import com.chisacwnu.hicwnu.data.model.WaterSegment

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chisacwnu.hicwnu.Destination
import com.chisacwnu.hicwnu.data.model.ScheduleGridStyle
import com.chisacwnu.hicwnu.ui.components.AdaptiveNavigationScaffold
import com.chisacwnu.hicwnu.ui.theme.LocalIsDarkTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.course_position_prefix
import hicwnu.shared.generated.resources.course_teacher_prefix
import hicwnu.shared.generated.resources.date_format_year_month_day
import hicwnu.shared.generated.resources.label_remark
import hicwnu.shared.generated.resources.status_semester_ended
import hicwnu.shared.generated.resources.text_no_courses_today
import hicwnu.shared.generated.resources.title_current_week
import hicwnu.shared.generated.resources.title_semester_not_set
import hicwnu.shared.generated.resources.title_today_schedule
import hicwnu.shared.generated.resources.title_vacation_until_start
import hicwnu.shared.generated.resources.week_days_full_names
import kotlin.time.Clock

private const val DEFAULT_TIME_ZERO = "00:00"
private const val EMPTY_TIME_PLACEHOLDER = "--:--"
private const val DEFAULT_DAYS_ZERO = "0"
private const val DEFAULT_OVERDUE_DAYS = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScheduleScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: TodayScheduleViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridStyle by viewModel.gridStyle.collectAsState()
    val isDark = LocalIsDarkTheme.current
    val dormInfo by viewModel.dormInfo.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    AdaptiveNavigationScaffold(
        currentDestination = Destination.TodaySchedule,
        onTabSelected = { dest -> onNavigate(dest) }
    ) { navPadding ->
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.title_today_schedule),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (val state = uiState) {
                    is TodayUiState.Loading -> {  }
                    is TodayUiState.Success -> {
                        TodayContent(state, gridStyle, isDark, navPadding, dormInfo.lightsOut, dormInfo.waterSegments, dormInfo.showLightsOut, dormInfo.showWater)
                    }
                }
            }
        }
    }
}

@Composable
fun TodayContent(
    state: TodayUiState.Success,
    gridStyle: ScheduleGridStyle,
    isDark: Boolean,
    navPadding: PaddingValues = PaddingValues(0.dp),
    lightsOut: String = "",
    waterSegments: List<WaterSegment> = emptyList(),
    showDormLightsOut: Boolean = true,
    showDormWater: Boolean = true
) {
    val currentTime = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time }

    val targetScrollIndex = remember(state.courses, currentTime) {
        val firstActiveIndex = state.courses.indexOfFirst { model ->
            try {
                LocalTime.parse(model.endTime ?: DEFAULT_TIME_ZERO) >= currentTime
            } catch (e: Exception) {
                true
            }
        }

        if (firstActiveIndex == -1) {
            (state.courses.size - 1).coerceAtLeast(0)
        } else {
            firstActiveIndex
        }
    }

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = targetScrollIndex
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val weekDays = stringArrayResource(Res.array.week_days_full_names)

        val year = state.today.year.toString()
        val month = state.today.month.number.toString()
        val day = state.today.day.toString()

        val formattedDate = stringResource(Res.string.date_format_year_month_day, year, month, day)

        val dateStr = remember(state.today, weekDays, formattedDate) {
            val weekDayName = weekDays.getOrNull(state.today.dayOfWeek.isoDayNumber - 1).orEmpty()
            "$formattedDate $weekDayName"
        }

        val subTitle = when (state.status) {
            TodayStatus.NoSemesterConfig -> stringResource(Res.string.title_semester_not_set)

            TodayStatus.Vacation -> {
                val days = if (state.startDate != null) {
                    state.today.daysUntil(state.startDate).toString()
                } else DEFAULT_DAYS_ZERO
                stringResource(Res.string.title_vacation_until_start, days)
            }

            TodayStatus.SemesterEnded -> {
                val overdueDays = if (state.startDate != null) {
                    val targetDayOfWeek = DayOfWeek(state.firstDayOfWeek)
                    val daysShift = (state.startDate.dayOfWeek.ordinal - targetDayOfWeek.ordinal + 7) % 7
                    val firstWeekStart = LocalDate.fromEpochDays(state.startDate.toEpochDays() - daysShift)

                    val semesterEndDate = LocalDate.fromEpochDays(
                        firstWeekStart.toEpochDays() + (state.totalWeeks * 7) - 1
                    )
                    semesterEndDate.daysUntil(state.today).coerceAtLeast(DEFAULT_OVERDUE_DAYS)
                } else {
                    DEFAULT_OVERDUE_DAYS
                }
                stringResource(Res.string.status_semester_ended, overdueDays)
            }

            TodayStatus.Normal -> stringResource(Res.string.title_current_week, state.weekIndex.toString())
        }

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = dateStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        }

        DormScheduleCard(lightsOut, waterSegments, showDormLightsOut, showDormWater)

        Spacer(modifier = Modifier.height(12.dp))

        if (state.courses.isEmpty()) {
            EmptyStateView()
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = navPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(state.courses) { _, model ->
                    CourseTimelineItem(model, gridStyle, isDark)
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun CourseTimelineItem(
    model: CourseDisplayModel,
    gridStyle: ScheduleGridStyle,
    isDark: Boolean
) {
    val currentTime = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time }
    val isFinished = remember(model.endTime, currentTime) {
        try {
            LocalTime.parse(model.endTime ?: DEFAULT_TIME_ZERO) < currentTime
        } catch (e: Exception) { false }
    }

    val colorPair = gridStyle.courseColorMaps.getOrElse(model.course.colorInt) {
        ScheduleGridStyle.DEFAULT_COLOR_MAPS[0]
    }
    val themeColor = if (isDark) colorPair.dark else colorPair.light

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = if (isFinished) 0.5f else 1f)
    ) {
        Column(
            modifier = Modifier.width(65.dp).padding(top = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = model.startTime ?: EMPTY_TIME_PLACEHOLDER,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    textDecoration = if (isFinished) TextDecoration.LineThrough else null
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = model.endTime ?: EMPTY_TIME_PLACEHOLDER,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = themeColor
                ),
                shape = MaterialTheme.shapes.medium,
                elevation = if (isFinished) CardDefaults.cardElevation(defaultElevation = 0.dp)
                else CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = model.course.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (isFinished) TextDecoration.LineThrough else null
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (model.course.position.isNotBlank()) {
                        Text(
                            text = stringResource(Res.string.course_position_prefix, model.course.position),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (model.course.teacher.isNotBlank()) {
                        Text(
                            text = stringResource(Res.string.course_teacher_prefix, model.course.teacher),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            model.course.remark?.takeIf { it.isNotBlank() }?.let { remark ->
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, start = 4.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.label_remark),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = remark,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.text_no_courses_today),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun DormScheduleCard(
    lightsOut: String,
    segments: List<WaterSegment>,
    showLightsOut: Boolean,
    showWater: Boolean
) {
    if (!showLightsOut && !showWater) return
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time

    fun toMinutes(value: String): Int {
        val parts = value.split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 60 +
            (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    val nowMinutes = now.hour * 60 + now.minute
    val activeColor = MaterialTheme.colorScheme.primary

    val waterText = buildAnnotatedString {
        segments.forEachIndexed { index, segment ->
            if (index > 0) append("\n")
            val startMinutes = toMinutes(segment.start)
            var endMinutes = toMinutes(segment.end)
            if (endMinutes <= startMinutes) endMinutes += 24 * 60
            val checkMinutes = if (nowMinutes < startMinutes) nowMinutes + 24 * 60 else nowMinutes
            val isActive = checkMinutes in startMinutes until endMinutes
            val label = segment.start + "–" + segment.end
            if (isActive) {
                withStyle(SpanStyle(color = activeColor, fontWeight = FontWeight.Bold)) {
                    append(label)
                }
            } else {
                append(label)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            if (showLightsOut) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.dorm_lights_out_tonight),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = lightsOut,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showLightsOut && showWater) {
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (showWater) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = stringResource(Res.string.dorm_hot_water),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = waterText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
