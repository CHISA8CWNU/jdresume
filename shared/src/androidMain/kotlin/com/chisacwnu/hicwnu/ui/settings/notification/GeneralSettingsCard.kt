/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.notification

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.desc_compat_wearable_sync
import hicwnu.shared.generated.resources.item_auto_mode
import hicwnu.shared.generated.resources.item_background_and_autostart
import hicwnu.shared.generated.resources.item_compat_wearable_sync
import hicwnu.shared.generated.resources.item_course_reminder
import hicwnu.shared.generated.resources.item_dnd_permission
import hicwnu.shared.generated.resources.item_exact_alarm_permission
import hicwnu.shared.generated.resources.item_ignore_battery_optimization
import hicwnu.shared.generated.resources.item_remind_time_before
import hicwnu.shared.generated.resources.remind_time_minutes_format
import hicwnu.shared.generated.resources.section_title_general
import hicwnu.shared.generated.resources.status_authorized
import hicwnu.shared.generated.resources.status_disabled
import hicwnu.shared.generated.resources.status_enabled
import hicwnu.shared.generated.resources.status_unauthorized
import hicwnu.shared.generated.resources.text_auto_mode_dependency
import hicwnu.shared.generated.resources.text_permission_importance_detail
import hicwnu.shared.generated.resources.text_permission_importance_title


@Composable
fun GeneralSettingsCard(
    uiState: NotificationSettingsUiState,
    currentModeText: String?,
    onReminderToggle: (Boolean) -> Unit,
    onCompatWearableToggle: (Boolean) -> Unit,
    onAutoModeClick: () -> Unit,
    onRemindTimeClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    onBatteryOptimizationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

        var showExactAlarmDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.section_title_general),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                    text = stringResource(Res.string.text_permission_importance_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.text_permission_importance_detail),
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.item_course_reminder),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = uiState.reminderEnabled,
                        onCheckedChange = { targetState ->
                            if (targetState) {
                                if (hasExactAlarmPermission(context)) {
                                    onReminderToggle(true)
                                } else {
                                    showExactAlarmDialog = true
                                }
                            } else {
                                onReminderToggle(false)
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.item_compat_wearable_sync),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = uiState.compatWearableSync,
                            onCheckedChange = onCompatWearableToggle
                        )
                    }
                    Text(
                        text = stringResource(Res.string.desc_compat_wearable_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }

                HorizontalDivider()

                                SettingItemRow(
                    title = stringResource(Res.string.item_auto_mode),
                    currentValue = currentModeText,
                    onClick = onAutoModeClick
                )
                if (!uiState.reminderEnabled) {
                    Text(
                        text = stringResource(Res.string.text_auto_mode_dependency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
                    )
                }

                HorizontalDivider()

                                SettingItemRow(
                    title = stringResource(Res.string.item_remind_time_before),
                    currentValue = stringResource(Res.string.remind_time_minutes_format, uiState.remindBeforeMinutes),
                    onClick = onRemindTimeClick
                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    HorizontalDivider()
                    val statusText = if (uiState.exactAlarmStatus)
                        stringResource(Res.string.status_enabled)
                    else
                        stringResource(Res.string.status_disabled)

                    SettingItemRow(
                        title = stringResource(Res.string.item_exact_alarm_permission),
                        currentValue = statusText,
                        onClick = { openExactAlarmSettings(context) }
                    )
                }

                HorizontalDivider()

                                val dndStatusText = if (uiState.dndPermissionStatus)
                    stringResource(Res.string.status_authorized)
                else
                    stringResource(Res.string.status_unauthorized)

                SettingItemRow(
                    title = stringResource(Res.string.item_dnd_permission),
                    currentValue = dndStatusText,
                    onClick = { openDndSettings(context) }
                )

                HorizontalDivider()

                                SettingItemRow(
                    title = stringResource(Res.string.item_background_and_autostart),
                    onClick = onAppSettingsClick
                )

                HorizontalDivider()

                                SettingItemRow(
                    title = stringResource(Res.string.item_ignore_battery_optimization),
                    onClick = onBatteryOptimizationClick
                )
            }
        }
    }

        if (showExactAlarmDialog) {
        ExactAlarmPermissionGuideDialog(
            onDismiss = { showExactAlarmDialog = false }
        )
    }
}