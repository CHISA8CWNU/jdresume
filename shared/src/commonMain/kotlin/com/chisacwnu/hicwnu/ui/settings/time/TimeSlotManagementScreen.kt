/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.time

import androidx.compose.foundation.background
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.action_cancel
import hicwnu.shared.generated.resources.action_confirm
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.preset_applied
import hicwnu.shared.generated.resources.preset_apply_message
import hicwnu.shared.generated.resources.preset_apply_title
import hicwnu.shared.generated.resources.preset_current_season
import hicwnu.shared.generated.resources.preset_custom_desc
import hicwnu.shared.generated.resources.preset_custom_title
import hicwnu.shared.generated.resources.preset_in_use
import hicwnu.shared.generated.resources.preset_selection_hint
import hicwnu.shared.generated.resources.toast_custom_time_applied
import hicwnu.shared.generated.resources.preset_summer_desc
import hicwnu.shared.generated.resources.preset_summer_title
import hicwnu.shared.generated.resources.preset_winter_desc
import hicwnu.shared.generated.resources.preset_winter_title
import hicwnu.shared.generated.resources.title_time_slot_management


enum class SchedulePreset { SUMMER, WINTER }

val SUMMER_SLOTS = listOf(
    TimeSlot(1, "08:00", "08:40", ""),
    TimeSlot(2, "08:50", "09:30", ""),
    TimeSlot(3, "09:45", "10:25", ""),
    TimeSlot(4, "10:35", "11:15", ""),
    TimeSlot(5, "11:25", "12:05", ""),
    TimeSlot(6, "15:00", "15:40", ""),
    TimeSlot(7, "15:50", "16:30", ""),
    TimeSlot(8, "16:40", "17:20", ""),
    TimeSlot(9, "17:30", "18:10", ""),
    TimeSlot(10, "19:30", "20:10", ""),
    TimeSlot(11, "20:20", "21:00", ""),
    TimeSlot(12, "21:10", "21:50", "")
)

val WINTER_SLOTS = listOf(
    TimeSlot(1, "08:00", "08:40", ""),
    TimeSlot(2, "08:50", "09:30", ""),
    TimeSlot(3, "09:45", "10:25", ""),
    TimeSlot(4, "10:35", "11:15", ""),
    TimeSlot(5, "11:25", "12:05", ""),
    TimeSlot(6, "14:30", "15:10", ""),
    TimeSlot(7, "15:20", "16:00", ""),
    TimeSlot(8, "16:10", "16:50", ""),
    TimeSlot(9, "17:00", "17:40", ""),
    TimeSlot(10, "19:00", "19:40", ""),
    TimeSlot(11, "19:50", "20:30", ""),
    TimeSlot(12, "20:40", "21:20", "")
)

private fun List<TimeSlot>.normalize() =
    sortedBy { it.number }.map { Triple(it.number, it.startTime, it.endTime) }


fun schedulePresetForDate(monthNumber: Int, dayOfMonth: Int): SchedulePreset {
    val isWinter = monthNumber >= 10 || monthNumber <= 4 || (monthNumber == 5 && dayOfMonth <= 1)
    return if (isWinter) SchedulePreset.WINTER else SchedulePreset.SUMMER
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotManagementScreen(
    onBack: () -> Unit,
    timeSlotViewModel: TimeSlotViewModel = koinViewModel()
) {
    val uiState by timeSlotViewModel.timeSlotsUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var pendingPreset by remember { mutableStateOf<SchedulePreset?>(null) }
    var showCustomEditorScreen by remember { mutableStateOf(false) }

        val activePreset = remember(uiState.timeSlots, uiState.isDataLoaded) {
        if (!uiState.isDataLoaded) return@remember null
        when (uiState.timeSlots.normalize()) {
            SUMMER_SLOTS.normalize() -> SchedulePreset.SUMMER
            WINTER_SLOTS.normalize() -> SchedulePreset.WINTER
            else -> null
        }
    }

        val seasonPreset = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        schedulePresetForDate(now.monthNumber, now.dayOfMonth)
    }
    val seasonLabel = stringResource(Res.string.preset_current_season)

    val summerTitle = stringResource(Res.string.preset_summer_title)
    val winterTitle = stringResource(Res.string.preset_winter_title)
    val customTitle = stringResource(Res.string.preset_custom_title)
    val customDesc = stringResource(Res.string.preset_custom_desc)
    val inUseLabel = stringResource(Res.string.preset_in_use)
    val customAppliedMsg = stringResource(Res.string.toast_custom_time_applied)

    if (showCustomEditorScreen) {
        CustomTimeSlotEditorScreen(
            initialSlots = uiState.timeSlots,
            onBack = { showCustomEditorScreen = false },
            onSave = { slots ->
                showCustomEditorScreen = false
                timeSlotViewModel.onSaveAllSettings(
                    timeSlots = slots,
                    classDuration = 40,
                    breakDuration = 10,
                    onSuccess = {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(customAppliedMsg)
                        }
                    }
                )
            }
        )
        return
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.title_time_slot_management)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.preset_selection_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            PresetCard(
                title = summerTitle,
                desc = stringResource(Res.string.preset_summer_desc),
                inUse = activePreset == SchedulePreset.SUMMER,
                inUseLabel = inUseLabel,
                isCurrentSeason = seasonPreset == SchedulePreset.SUMMER,
                seasonLabel = seasonLabel,
                onClick = { pendingPreset = SchedulePreset.SUMMER }
            )
            Spacer(Modifier.height(12.dp))
            PresetCard(
                title = winterTitle,
                desc = stringResource(Res.string.preset_winter_desc),
                inUse = activePreset == SchedulePreset.WINTER,
                inUseLabel = inUseLabel,
                isCurrentSeason = seasonPreset == SchedulePreset.WINTER,
                seasonLabel = seasonLabel,
                onClick = { pendingPreset = SchedulePreset.WINTER }
            )
            Spacer(Modifier.height(12.dp))
            PresetCard(
                title = customTitle,
                desc = customDesc,
                inUse = activePreset == null,
                inUseLabel = inUseLabel,
                isCurrentSeason = false,
                seasonLabel = seasonLabel,
                onClick = { showCustomEditorScreen = true }
            )
        }
    }

        pendingPreset?.let { preset ->
        val presetName = if (preset == SchedulePreset.SUMMER) summerTitle else winterTitle
        val appliedMessage = stringResource(Res.string.preset_applied, presetName)
        AlertDialog(
            onDismissRequest = { pendingPreset = null },
            title = { Text(stringResource(Res.string.preset_apply_title)) },
            text = { Text(stringResource(Res.string.preset_apply_message, presetName)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingPreset = null
                    val slots = if (preset == SchedulePreset.SUMMER) SUMMER_SLOTS else WINTER_SLOTS
                    timeSlotViewModel.onSaveAllSettings(
                        timeSlots = slots,
                        classDuration = 40,
                        breakDuration = 10,
                        onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(appliedMessage)
                            }
                        }
                    )
                }) {
                    Text(stringResource(Res.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPreset = null }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }

}

@Composable
private fun PresetCard(
    title: String,
    desc: String,
    inUse: Boolean,
    inUseLabel: String,
    isCurrentSeason: Boolean,
    seasonLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isCurrentSeason) {
                        Text(
                            text = seasonLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    if (inUse) {
                        Text(
                            text = inUseLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}