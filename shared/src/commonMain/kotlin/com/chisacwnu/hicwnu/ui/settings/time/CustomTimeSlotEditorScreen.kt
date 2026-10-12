/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.time

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.ui.components.NativeNumberPicker
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.banner_early_study
import hicwnu.shared.generated.resources.editor_back
import hicwnu.shared.generated.resources.editor_cancel
import hicwnu.shared.generated.resources.editor_done
import hicwnu.shared.generated.resources.editor_enable_early_study_hint
import hicwnu.shared.generated.resources.editor_end_time
import hicwnu.shared.generated.resources.editor_period_label
import hicwnu.shared.generated.resources.editor_save
import hicwnu.shared.generated.resources.editor_start_time
import hicwnu.shared.generated.resources.editor_time_separator
import hicwnu.shared.generated.resources.title_custom_time_slots

@Composable
fun CustomTimeSlotEditorScreen(
    initialSlots: List<TimeSlot>,
    onBack: () -> Unit,
    onSave: (List<TimeSlot>) -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    val slots = remember {
        if (initialSlots.any { it.number > 0 }) {

            val regular = initialSlots.filter { it.number > 0 }.sortedBy { it.number }
            mutableStateListOf(*regular.toTypedArray())
        } else {

            mutableStateListOf(*SUMMER_SLOTS.toTypedArray())
        }
    }

    val early = initialSlots.firstOrNull { it.number == 0 }
    var earlyStudyEnabled by remember { mutableStateOf(early != null) }
    var earlyStudyStart by remember { mutableStateOf(early?.startTime ?: "07:00") }
    var earlyStudyEnd by remember { mutableStateOf(early?.endTime ?: "07:40") }

    var editing by remember { mutableStateOf<EditTarget?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.title_custom_time_slots)) },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text(stringResource(Res.string.editor_back), color = scheme.primary)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    val earlyStudyLabel = stringResource(Res.string.banner_early_study)
                    val startTimeLabel = stringResource(Res.string.editor_start_time)
                    val endTimeLabel = stringResource(Res.string.editor_end_time)
                    Text(
                        stringResource(Res.string.editor_enable_early_study_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(scheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.banner_early_study), style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = earlyStudyEnabled,
                            onCheckedChange = { earlyStudyEnabled = it }
                        )
                    }

                    if (earlyStudyEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TimeSlotCard(
                            label = earlyStudyLabel,
                            start = earlyStudyStart,
                            end = earlyStudyEnd,
                            onPickStart = {
                                val (h, m) = parseTime(earlyStudyStart)
                                editing = EditTarget(
                                    key = "early-start",
                                    title = earlyStudyLabel + " · " + startTimeLabel,
                                    initialHour = h,
                                    initialMinute = m,
                                    onApply = { nh, nm -> earlyStudyStart = fmt(nh, nm) }
                                )
                            },
                            onPickEnd = {
                                val (h, m) = parseTime(earlyStudyEnd)
                                editing = EditTarget(
                                    key = "early-end",
                                    title = earlyStudyLabel + " · " + endTimeLabel,
                                    initialHour = h,
                                    initialMinute = m,
                                    onApply = { nh, nm -> earlyStudyEnd = fmt(nh, nm) }
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    for (i in slots.indices) {
                        val periodLabel = stringResource(Res.string.editor_period_label, i + 1)
                        TimeSlotCard(
                            label = stringResource(Res.string.editor_period_label, i + 1),
                            start = slots[i].startTime,
                            end = slots[i].endTime,
                            onPickStart = {
                                val (h, m) = parseTime(slots[i].startTime)
                                editing = EditTarget(
                                    key = "start-$i",
                                    title = periodLabel + " · " + startTimeLabel,
                                    initialHour = h,
                                    initialMinute = m,
                                    onApply = { nh, nm ->
                                        slots[i] = slots[i].copy(startTime = fmt(nh, nm))
                                        recomputeChain(slots, i, changedStart = true)
                                    }
                                )
                            },
                            onPickEnd = {
                                val (h, m) = parseTime(slots[i].endTime)
                                editing = EditTarget(
                                    key = "end-$i",
                                    title = periodLabel + " · " + endTimeLabel,
                                    initialHour = h,
                                    initialMinute = m,
                                    onApply = { nh, nm ->
                                        slots[i] = slots[i].copy(endTime = fmt(nh, nm))
                                        recomputeChain(slots, i, changedStart = false)
                                    }
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Button(
                    onClick = {
                        val allSlots = mutableListOf<TimeSlot>()
                        if (earlyStudyEnabled) {
                            allSlots.add(TimeSlot(0, earlyStudyStart, earlyStudyEnd, ""))
                        }
                        allSlots.addAll(
                            slots.mapIndexed { index, slot -> slot.copy(number = index + 1) }
                        )
                        onSave(allSlots)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(Res.string.editor_save))
                }
            }
        }

        if (editing != null) {
            val target = editing!!
            TimePickerDialog(
                target = target,
                onConfirm = { h, m ->
                    target.onApply(h, m)
                    editing = null
                },
                onDismiss = { editing = null }
            )
        }
    }
}

@Composable
private fun TimeSlotCard(
    label: String,
    start: String,
    end: String,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.width(72.dp)
        )
        TimeChip(text = start, onClick = onPickStart)
        Text(
            " ${stringResource(Res.string.editor_time_separator)} ",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        TimeChip(text = end, onClick = onPickEnd)
    }
}

@Composable
private fun TimeChip(text: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(scheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = scheme.primary
        )
    }
}

@Composable
private fun TimePickerDialog(
    target: EditTarget,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hourValues = remember { (0..23).toList() }
    val minuteValues = remember { (0..59 step 5).toList() }

    var hour by remember(target.key) { mutableStateOf(target.initialHour) }
    var minute by remember(target.key) { mutableStateOf(target.initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(target.title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NativeNumberPicker(
                    values = hourValues,
                    selectedValue = hour,
                    onValueChange = { hour = it },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    ":",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                NativeNumberPicker(
                    values = minuteValues,
                    selectedValue = snapMinute(minute),
                    onValueChange = { minute = it },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(hour, minute) }) {
                Text(stringResource(Res.string.editor_done))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.editor_cancel))
            }
        }
    )
}

private class EditTarget(
    val key: String,
    val title: String,
    val initialHour: Int,
    val initialMinute: Int,
    val onApply: (Int, Int) -> Unit
)

private fun snapMinute(minute: Int): Int = ((minute + 2) / 5 * 5).coerceIn(0, 55)

private fun parseTime(s: String): Pair<Int, Int> {
    val parts = s.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h to m
}

private fun fmt(h: Int, m: Int): String = "%02d:%02d".format(h, m)

private fun addMinutes(h: Int, m: Int, addMin: Int): Pair<Int, Int> {
    val total = h * 60 + m + addMin
    val nh = ((total / 60) % 24 + 24) % 24
    val nm = ((total % 60) + 60) % 60
    return nh to nm
}

private fun breakBeforePeriod(j: Int): Int = if (j == 2) 15 else 10

private fun recomputeChain(
    slots: MutableList<TimeSlot>,
    changedIndex: Int,
    changedStart: Boolean
) {
    if (changedIndex < 0 || changedIndex >= slots.size) return
    val anchorEnd = if (changedStart) {
        val (h, m) = parseTime(slots[changedIndex].startTime)
        val (eh, em) = addMinutes(h, m, 40)
        val newEnd = fmt(eh, em)
        slots[changedIndex] = slots[changedIndex].copy(endTime = newEnd)
        newEnd
    } else {
        slots[changedIndex].endTime
    }
    var prevEnd = anchorEnd
    for (j in (changedIndex + 1) until slots.size) {
        val (sh, sm) = if (j == 9) {

            val (h6, m6) = parseTime(slots[5].startTime)
            addMinutes(h6, m6, 270)
        } else {
            val (ph, pm) = parseTime(prevEnd)
            addMinutes(ph, pm, breakBeforePeriod(j))
        }
        val newStart = fmt(sh, sm)
        slots[j] = slots[j].copy(startTime = newStart)
        val (eh, em) = addMinutes(sh, sm, 40)
        val newEnd = fmt(eh, em)
        slots[j] = slots[j].copy(endTime = newEnd)
        prevEnd = newEnd
    }
}
