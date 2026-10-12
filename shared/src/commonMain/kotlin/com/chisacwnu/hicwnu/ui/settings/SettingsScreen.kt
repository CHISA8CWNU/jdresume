/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings
import hicwnu.shared.generated.resources.desc_dorm_show_water
import hicwnu.shared.generated.resources.item_dorm_show_water
import hicwnu.shared.generated.resources.desc_dorm_show_lights_out
import hicwnu.shared.generated.resources.item_dorm_show_lights_out
import hicwnu.shared.generated.resources.desc_dorm_water
import hicwnu.shared.generated.resources.desc_dorm_lights_out
import hicwnu.shared.generated.resources.dorm_section_water
import hicwnu.shared.generated.resources.dorm_section_lights_out
import hicwnu.shared.generated.resources.dorm_end_time
import hicwnu.shared.generated.resources.dorm_start_time
import hicwnu.shared.generated.resources.item_water_evening
import hicwnu.shared.generated.resources.item_water_noon
import hicwnu.shared.generated.resources.item_water_morning
import hicwnu.shared.generated.resources.item_dorm_winter_weekend
import hicwnu.shared.generated.resources.item_dorm_winter_weekday
import hicwnu.shared.generated.resources.item_dorm_summer_weekend
import hicwnu.shared.generated.resources.item_dorm_summer_weekday
import hicwnu.shared.generated.resources.section_title_dorm
import androidx.compose.material3.TextButton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import com.chisacwnu.hicwnu.data.model.DormSchedule
import com.chisacwnu.hicwnu.ui.components.NativeNumberPicker
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.Destination
import com.chisacwnu.hicwnu.ui.components.AdaptiveNavigationScaffold
import com.chisacwnu.hicwnu.ui.components.DatePickerModal
import com.chisacwnu.hicwnu.ui.components.QQ_GROUP_JOIN_URL
import com.chisacwnu.hicwnu.ui.components.NativeNumberPicker
import com.chisacwnu.hicwnu.ui.settings.additional.StartScreenSelectionDialog
import com.chisacwnu.hicwnu.ui.update.AppUpdateViewModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.desc_check_update
import hicwnu.shared.generated.resources.item_check_update
import hicwnu.shared.generated.resources.desc_about_app
import hicwnu.shared.generated.resources.desc_join_qq_channel
import hicwnu.shared.generated.resources.desc_join_qq_group
import hicwnu.shared.generated.resources.desc_language_settings
import hicwnu.shared.generated.resources.desc_theme_settings
import hicwnu.shared.generated.resources.item_about_app
import hicwnu.shared.generated.resources.item_join_qq_channel
import hicwnu.shared.generated.resources.item_join_qq_group
import hicwnu.shared.generated.resources.item_easter_egg_tools
import hicwnu.shared.generated.resources.desc_easter_egg_tools
import hicwnu.shared.generated.resources.item_language_settings
import hicwnu.shared.generated.resources.item_start_screen_settings
import hicwnu.shared.generated.resources.theme_settings_title
import hicwnu.shared.generated.resources.action_cancel
import hicwnu.shared.generated.resources.action_confirm
import hicwnu.shared.generated.resources.chevron_right_24px
import hicwnu.shared.generated.resources.date_format_year_month_day
import hicwnu.shared.generated.resources.day_of_week_monday
import hicwnu.shared.generated.resources.day_of_week_sunday
import hicwnu.shared.generated.resources.desc_course_conversion
import hicwnu.shared.generated.resources.desc_course_management
import hicwnu.shared.generated.resources.desc_first_day_of_week
import hicwnu.shared.generated.resources.desc_manage_course_tables
import hicwnu.shared.generated.resources.desc_more_options
import hicwnu.shared.generated.resources.desc_notification_settings
import hicwnu.shared.generated.resources.desc_personalization
import hicwnu.shared.generated.resources.desc_quick_actions
import hicwnu.shared.generated.resources.desc_set_start_date
import hicwnu.shared.generated.resources.desc_show_non_current_week
import hicwnu.shared.generated.resources.desc_show_weekends
import hicwnu.shared.generated.resources.desc_time_slot_customization
import hicwnu.shared.generated.resources.desc_total_weeks
import hicwnu.shared.generated.resources.dialog_title_manual_set_week
import hicwnu.shared.generated.resources.dialog_title_select_total_weeks
import hicwnu.shared.generated.resources.dialog_title_set_first_day_of_week
import hicwnu.shared.generated.resources.item_course_conversion
import hicwnu.shared.generated.resources.item_course_management
import hicwnu.shared.generated.resources.item_first_day_of_week
import hicwnu.shared.generated.resources.item_more_options
import hicwnu.shared.generated.resources.item_personalization
import hicwnu.shared.generated.resources.item_quick_actions
import hicwnu.shared.generated.resources.item_set_start_date
import hicwnu.shared.generated.resources.item_show_non_current_week
import hicwnu.shared.generated.resources.item_show_weekends
import hicwnu.shared.generated.resources.item_time_slot_customization
import hicwnu.shared.generated.resources.item_total_weeks
import hicwnu.shared.generated.resources.more_horiz_24px
import hicwnu.shared.generated.resources.section_title_advanced_features
import hicwnu.shared.generated.resources.section_title_basic
import hicwnu.shared.generated.resources.section_title_data
import hicwnu.shared.generated.resources.section_title_appearance
import hicwnu.shared.generated.resources.section_title_general_about
import hicwnu.shared.generated.resources.section_title_general_settings
import hicwnu.shared.generated.resources.status_not_set
import hicwnu.shared.generated.resources.status_set_start_date_first
import hicwnu.shared.generated.resources.status_total_weeks_format
import hicwnu.shared.generated.resources.title_course_notification_settings
import hicwnu.shared.generated.resources.title_manage_course_tables
import hicwnu.shared.generated.resources.title_schedule_settings

private val SETTING_PADDING = 16.dp
private val SECTION_SPACING = 16.dp
private val ITEM_SPACING = 16.dp

private const val QQ_CHANNEL_URL = "https://pd.qq.com/s/gnr0b13fd?b=5"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    updateViewModel: AppUpdateViewModel,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val uriHandler = LocalUriHandler.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    AdaptiveNavigationScaffold(
        currentDestination = Destination.Settings,
        onTabSelected = { dest -> onNavigate(dest) }
    ) { navPadding ->
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.title_schedule_settings),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            if (!uiState.isReady) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding))
            } else {
                val appSettings = uiState.appSettings
                val courseTableConfig = uiState.courseConfig
                val showWeekends = courseTableConfig?.showWeekends ?: true
                val semesterStartDateString = courseTableConfig?.semesterStartDate
                val semesterTotalWeeks = courseTableConfig?.semesterTotalWeeks ?: 20
                val firstDayOfWeekInt = courseTableConfig?.firstDayOfWeek ?: DayOfWeek.MONDAY.isoDayNumber

                val semesterStartDate: LocalDate? = remember(semesterStartDateString) {
                    semesterStartDateString?.let {
                        try {
                            LocalDate.parse(it)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                var showTotalWeeksDialog by remember { mutableStateOf(false) }
                var showDatePickerModal by remember { mutableStateOf(false) }
                var showFirstDayOfWeekDialog by remember { mutableStateOf(false) }
                var showStartScreenDialog by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = SETTING_PADDING),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
                    contentPadding = PaddingValues(
                        bottom = navPadding.calculateBottomPadding()
                    )
                ) {
                    item {
                        SettingsGroupSection(title = stringResource(Res.string.section_title_basic)) {
                            SettingItem(
                                title = stringResource(Res.string.item_set_start_date),
                                subtitle = stringResource(Res.string.desc_set_start_date),
                                onClick = { showDatePickerModal = true }
                            ) {
                                val formattedDate = semesterStartDate?.let {
                                    stringResource(
                                        Res.string.date_format_year_month_day,
                                        it.year.toString(),
                                        it.month.number.toString(),
                                        it.day.toString()
                                    )
                                } ?: stringResource(Res.string.status_not_set)

                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            SettingItem(
                                title = stringResource(Res.string.title_manage_course_tables),
                                subtitle = stringResource(Res.string.desc_manage_course_tables),
                                onClick = { onNavigate(Destination.ManageCourseTables) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.title_course_notification_settings),
                                subtitle = stringResource(Res.string.desc_notification_settings),
                                onClick = { onNavigate(Destination.NotificationSettings) }
                            )
                        }
                    }

                    item {
                        SettingsGroupSection(title = stringResource(Res.string.section_title_data)) {
                            SettingItem(
                                title = stringResource(Res.string.item_course_conversion),
                                subtitle = stringResource(Res.string.desc_course_conversion),
                                onClick = { onNavigate(Destination.CourseTableConversion()) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_course_management),
                                subtitle = stringResource(Res.string.desc_course_management),
                                onClick = { onNavigate(Destination.CourseManagementList) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_time_slot_customization),
                                subtitle = stringResource(Res.string.desc_time_slot_customization),
                                onClick = { onNavigate(Destination.TimeSlotSettings) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_quick_actions),
                                subtitle = stringResource(Res.string.desc_quick_actions),
                                onClick = { onNavigate(Destination.QuickActions) }
                            )
                        }
                    }

                    item {
                        SettingsGroupSection(title = stringResource(Res.string.section_title_appearance)) {
                            SettingItem(
                                title = stringResource(Res.string.item_show_weekends),
                                subtitle = stringResource(Res.string.desc_show_weekends)
                            ) {
                                Switch(
                                    checked = showWeekends,
                                    onCheckedChange = { viewModel.onShowWeekendsChanged(it) },
                                    colors = appSwitchColors()
                                )
                            }
                            SettingItem(
                                title = stringResource(Res.string.item_show_non_current_week),
                                subtitle = stringResource(Res.string.desc_show_non_current_week)
                            ) {
                                Switch(
                                    checked = appSettings.showNonCurrentWeekCourses,
                                    onCheckedChange = { viewModel.onShowNonCurrentWeekChanged(it) },
                                    colors = appSwitchColors()
                                )
                            }
                            SettingItem(
                                title = stringResource(Res.string.item_personalization),
                                subtitle = stringResource(Res.string.desc_personalization),
                                onClick = { onNavigate(Destination.StyleSettings) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.theme_settings_title),
                                subtitle = stringResource(Res.string.desc_theme_settings),
                                onClick = { onNavigate(Destination.ThemeSettings()) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_start_screen_settings),
                                subtitle = stringResource(uiState.appSettings.startScreen.labelRes),
                                onClick = { showStartScreenDialog = true }
                            )
                        }
                    }

                    item {
                        SettingsGroupSection(title = stringResource(Res.string.section_title_dorm)) {
                            SettingItem(
                                title = stringResource(Res.string.dorm_section_lights_out),
                                subtitle = stringResource(Res.string.desc_dorm_lights_out),
                                onClick = { onNavigate(Destination.DormLightsOut) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.dorm_section_water),
                                subtitle = stringResource(Res.string.desc_dorm_water),
                                onClick = { onNavigate(Destination.DormWater) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_dorm_show_lights_out),
                                subtitle = stringResource(Res.string.desc_dorm_show_lights_out)
                            ) {
                                Switch(
                                    checked = appSettings.showDormLightsOut,
                                    onCheckedChange = { viewModel.onShowDormLightsOutChanged(it) },
                                    colors = appSwitchColors()
                                )
                            }
                            SettingItem(
                                title = stringResource(Res.string.item_dorm_show_water),
                                subtitle = stringResource(Res.string.desc_dorm_show_water)
                            ) {
                                Switch(
                                    checked = appSettings.showDormWater,
                                    onCheckedChange = { viewModel.onShowDormWaterChanged(it) },
                                    colors = appSwitchColors()
                                )
                            }
                        }
                    }

                    item {
                        SettingsGroupSection(title = stringResource(Res.string.section_title_general_about)) {
                            SettingItem(
                                title = stringResource(Res.string.item_language_settings),
                                subtitle = stringResource(Res.string.desc_language_settings),
                                onClick = { onNavigate(Destination.LanguageSettings()) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_about_app),
                                subtitle = stringResource(Res.string.desc_about_app),
                                onClick = { onNavigate(Destination.About) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_join_qq_channel),
                                subtitle = stringResource(Res.string.desc_join_qq_channel),
                                onClick = { uriHandler.openUri(QQ_CHANNEL_URL) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_join_qq_group),
                                subtitle = stringResource(Res.string.desc_join_qq_group),
                                onClick = { uriHandler.openUri(QQ_GROUP_JOIN_URL) }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_check_update),
                                subtitle = stringResource(Res.string.desc_check_update),
                                onClick = { updateViewModel.onManualCheckRequested() }
                            )
                            SettingItem(
                                title = stringResource(Res.string.item_easter_egg_tools),
                                subtitle = stringResource(Res.string.desc_easter_egg_tools),
                                onClick = { onNavigate(Destination.PcTools) }
                            )
                        }
                    }
                }

                if (showDatePickerModal) {
                    DatePickerModal(
                        onDateSelected = { selectedDateMillis ->
                            viewModel.onSemesterStartDateSelected(selectedDateMillis)
                        },
                        onDismiss = { showDatePickerModal = false }
                    )
                }

                if (showTotalWeeksDialog) {
                    NumberPickerDialog(
                        title = stringResource(Res.string.dialog_title_select_total_weeks),
                        range = 1..30,
                        initialValue = semesterTotalWeeks,
                        onDismiss = { showTotalWeeksDialog = false },
                        onConfirm = { selectedWeeks ->
                            viewModel.onSemesterTotalWeeksSelected(selectedWeeks)
                            showTotalWeeksDialog = false
                        }
                    )
                }

                if (showFirstDayOfWeekDialog) {
                    DayOfWeekPickerDialog(
                        initialDayOfWeekInt = firstDayOfWeekInt,
                        onDismiss = { showFirstDayOfWeekDialog = false },
                        onConfirm = { selectedDayInt ->
                            viewModel.onFirstDayOfWeekSelected(selectedDayInt)
                            showFirstDayOfWeekDialog = false
                        }
                    )
                }

                StartScreenSelectionDialog(
                    showDialog = showStartScreenDialog,
                    currentSelected = uiState.appSettings.startScreen,
                    onDismiss = { showStartScreenDialog = false },
                    onConfirm = {
                        viewModel.onStartScreenChanged(it)
                        showStartScreenDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = SETTING_PADDING, bottom = 10.dp, top = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(SETTING_PADDING),
                verticalArrangement = Arrangement.spacedBy(ITEM_SPACING),
                content = content
            )
        }
    }
}

@Composable
private fun appSwitchColors() = SwitchDefaults.colors(
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    checkedIconColor = MaterialTheme.colorScheme.onPrimary
)

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector = vectorResource(Res.drawable.chevron_right_24px),
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = { Icon(icon, contentDescription = null) }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailingContent()
    }
}

@Composable
fun NumberPickerDialog(
    title: String,
    range: IntRange,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var dialogSelectedValue by remember { mutableIntStateOf(initialValue.coerceIn(range)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            NativeNumberPicker(
                values = range.toList(),
                selectedValue = initialValue.coerceIn(range),
                onValueChange = { newValue ->
                    dialogSelectedValue = newValue
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(dialogSelectedValue) }) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Composable
fun DayOfWeekPickerDialog(
    initialDayOfWeekInt: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val dayOfWeekMondayText = stringResource(Res.string.day_of_week_monday)
    val dayOfWeekSundayText = stringResource(Res.string.day_of_week_sunday)

    val dayOptionsMap = mapOf(
        dayOfWeekMondayText to DayOfWeek.MONDAY.isoDayNumber,
        dayOfWeekSundayText to DayOfWeek.SUNDAY.isoDayNumber
    )
    val dayOptions = dayOptionsMap.keys.toList()

    val initialSelectedDayText = dayOptionsMap.entries.firstOrNull { it.value == initialDayOfWeekInt }?.key
        ?: dayOfWeekMondayText

    var dialogSelectedText by remember { mutableStateOf(initialSelectedDayText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.dialog_title_set_first_day_of_week)) },
        text = {
            NativeNumberPicker(
                values = dayOptions,
                selectedValue = dialogSelectedText,
                onValueChange = { newValue ->
                    dialogSelectedText = newValue
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val selectedDayInt = dayOptionsMap[dialogSelectedText] ?: DayOfWeek.MONDAY.isoDayNumber
                onConfirm(selectedDayInt)
            }) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}
