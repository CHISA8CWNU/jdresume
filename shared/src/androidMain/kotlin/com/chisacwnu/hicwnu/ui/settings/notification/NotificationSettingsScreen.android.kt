/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chisacwnu.hicwnu.data.model.AutoControlMode
import com.chisacwnu.hicwnu.ui.components.ToastManager
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.auto_mode_dnd
import hicwnu.shared.generated.resources.auto_mode_off
import hicwnu.shared.generated.resources.auto_mode_silent
import hicwnu.shared.generated.resources.toast_enable_reminder_first
import hicwnu.shared.generated.resources.toast_notification_permission_denied


@Composable
actual fun PlatformGeneralSettingsSection(
    uiState: NotificationSettingsUiState,
    viewModel: NotificationSettingsViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

        val permissionDeniedMessage = stringResource(Res.string.toast_notification_permission_denied)
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            ToastManager.show(permissionDeniedMessage)
        }
    }

        DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.updateExactAlarmStatus(hasExactAlarmPermission(context))
                viewModel.updateDndPermissionStatus(hasDndPermission(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

        LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission(context)) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val currentModeText = if (!uiState.autoModeEnabled) {
        stringResource(Res.string.auto_mode_off)
    } else {
        when (uiState.autoControlMode) {
            AutoControlMode.DND -> stringResource(Res.string.auto_mode_dnd)
            AutoControlMode.SILENT -> stringResource(Res.string.auto_mode_silent)
        }
    }

    val enableReminderToast = stringResource(Res.string.toast_enable_reminder_first)

    GeneralSettingsCard(
        uiState = uiState,
        currentModeText = currentModeText,
        onReminderToggle = { isEnabled ->
            viewModel.updateReminderEnabled(isEnabled)
        },
        onCompatWearableToggle = { isEnabled ->
            viewModel.updateCompatWearableSync(isEnabled)
        },
        onAutoModeClick = {
            if (uiState.reminderEnabled) {
                viewModel.showDialog(NotificationDialogType.AutoModeSelection)
            } else {
                ToastManager.show(enableReminderToast)
            }
        },
        onRemindTimeClick = { viewModel.showDialog(NotificationDialogType.EditRemindMinutes) },
        onAppSettingsClick = { openAppSettings(context) },
        onBatteryOptimizationClick = { openIgnoreBatteryOptimizationSettings(context) }
    )
}


@Composable
actual fun PlatformNotificationDialogDispatcher(
    uiState: NotificationSettingsUiState,
    viewModel: NotificationSettingsViewModel
) {
    NotificationDialogDispatcher(
        uiState = uiState,
        viewModel = viewModel
    )
}