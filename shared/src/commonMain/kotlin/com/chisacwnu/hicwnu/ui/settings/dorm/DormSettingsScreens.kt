/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.dorm
import hicwnu.shared.generated.resources.action_confirm
import hicwnu.shared.generated.resources.action_cancel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.ui.components.NativeNumberPicker
import com.chisacwnu.hicwnu.ui.settings.SettingsViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.chevron_right_24px
import hicwnu.shared.generated.resources.dorm_end_time
import hicwnu.shared.generated.resources.dorm_section_lights_out
import hicwnu.shared.generated.resources.dorm_section_water
import hicwnu.shared.generated.resources.dorm_start_time
import hicwnu.shared.generated.resources.item_dorm_summer_weekday
import hicwnu.shared.generated.resources.item_dorm_summer_weekend
import hicwnu.shared.generated.resources.item_dorm_winter_weekday
import hicwnu.shared.generated.resources.item_dorm_winter_weekend
import hicwnu.shared.generated.resources.item_water_evening
import hicwnu.shared.generated.resources.item_water_morning
import hicwnu.shared.generated.resources.item_water_noon

private val PAGE_PADDING = 16.dp
private val ROW_SPACING = 12.dp

@Composable
fun DormLightsOutScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appSettings = uiState.appSettings
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val titles = listOf(
        Res.string.item_dorm_summer_weekday,
        Res.string.item_dorm_summer_weekend,
        Res.string.item_dorm_winter_weekday,
        Res.string.item_dorm_winter_weekend
    )
    val values = listOf(
        appSettings.dormSummerWeekdayOff,
        appSettings.dormSummerWeekendOff,
        appSettings.dormWinterWeekdayOff,
        appSettings.dormWinterWeekendOff
    )

    DormDetailScreen(
        title = stringResource(Res.string.dorm_section_lights_out),
        onBack = onBack
    ) {
        titles.forEachIndexed { index, title ->
            DormDetailRow(title = stringResource(title), value = values[index]) {
                editingIndex = index
            }
        }
    }

    editingIndex?.let { index ->
        DormTimeDialog(
            title = stringResource(titles[index]),
            startLabel = stringResource(Res.string.dorm_start_time),
            endLabel = null,
            start = values[index],
            end = null,
            onConfirm = { start, _ ->
                when (index) {
                    0 -> viewModel.onDormSummerWeekdayOffChanged(start)
                    1 -> viewModel.onDormSummerWeekendOffChanged(start)
                    2 -> viewModel.onDormWinterWeekdayOffChanged(start)
                    else -> viewModel.onDormWinterWeekendOffChanged(start)
                }
                editingIndex = null
            },
            onDismiss = { editingIndex = null }
        )
    }
}

@Composable
fun DormWaterScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val appSettings = uiState.appSettings
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val titles = listOf(
        Res.string.item_water_morning,
        Res.string.item_water_noon,
        Res.string.item_water_evening
    )
    val starts = listOf(
        appSettings.waterMorningStart,
        appSettings.waterNoonStart,
        appSettings.waterEveningStart
    )
    val ends = listOf(
        appSettings.waterMorningEnd,
        appSettings.waterNoonEnd,
        null
    )
    val values = listOf(
        starts[0] + "–" + ends[0],
        starts[1] + "–" + ends[1],
        starts[2]
    )

    DormDetailScreen(
        title = stringResource(Res.string.dorm_section_water),
        onBack = onBack
    ) {
        titles.forEachIndexed { index, title ->
            DormDetailRow(title = stringResource(title), value = values[index]) {
                editingIndex = index
            }
        }
    }

    editingIndex?.let { index ->
        DormTimeDialog(
            title = stringResource(titles[index]),
            startLabel = stringResource(Res.string.dorm_start_time),
            endLabel = ends[index]?.let { stringResource(Res.string.dorm_end_time) },
            start = starts[index],
            end = ends[index],
            onConfirm = { start, end ->
                when (index) {
                    0 -> {
                        viewModel.onWaterMorningStartChanged(start)
                        viewModel.onWaterMorningEndChanged(end)
                    }

                    1 -> {
                        viewModel.onWaterNoonStartChanged(start)
                        viewModel.onWaterNoonEndChanged(end)
                    }

                    else -> viewModel.onWaterEveningStartChanged(start)
                }
                editingIndex = null
            },
            onDismiss = { editingIndex = null }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DormDetailScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PAGE_PADDING),
            verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(PAGE_PADDING),
                    verticalArrangement = Arrangement.spacedBy(ROW_SPACING),
                    content = { content() }
                )
            }
        }
    }
}


@Composable
private fun DormDetailRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = vectorResource(Res.drawable.chevron_right_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}


@Composable
private fun DormTimeDialog(
    title: String,
    startLabel: String,
    endLabel: String?,
    start: String,
    end: String?,
    onConfirm: (start: String, end: String) -> Unit,
    onDismiss: () -> Unit
) {
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59 step 5).toList() }

    fun split(value: String): Pair<Int, Int> {
        val parts = value.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h.coerceIn(0, 23) to (m / 5 * 5).coerceIn(0, 55)
    }

    val initialStart = split(start)
    val initialEnd = split(end ?: start)

    var startHour by remember(title) { mutableStateOf(initialStart.first) }
    var startMinute by remember(title) { mutableStateOf(initialStart.second) }
    var endHour by remember(title) { mutableStateOf(initialEnd.first) }
    var endMinute by remember(title) { mutableStateOf(initialEnd.second) }

    fun fmt(h: Int, m: Int): String =
        h.toString().padStart(2, '0') + ":" + m.toString().padStart(2, '0')

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DormTimePickerRow(
                    hours = hours,
                    minutes = minutes,
                    label = startLabel,
                    hour = startHour,
                    minute = startMinute,
                    onHourChange = { startHour = it },
                    onMinuteChange = { startMinute = it }
                )
                if (endLabel != null) {
                    DormTimePickerRow(
                        hours = hours,
                        minutes = minutes,
                        label = endLabel,
                        hour = endHour,
                        minute = endMinute,
                        onHourChange = { endHour = it },
                        onMinuteChange = { endMinute = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(fmt(startHour, startMinute), fmt(endHour, endMinute))
            }) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}


@Composable
private fun DormTimePickerRow(
    hours: List<Int>,
    minutes: List<Int>,
    label: String,
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NativeNumberPicker(
                values = hours,
                selectedValue = hour,
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f)
            )
            Text(":", style = MaterialTheme.typography.titleMedium)
            NativeNumberPicker(
                values = minutes,
                selectedValue = minute,
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
