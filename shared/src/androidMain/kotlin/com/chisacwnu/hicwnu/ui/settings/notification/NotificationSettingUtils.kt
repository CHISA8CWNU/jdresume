/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri



fun hasExactAlarmPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService<AlarmManager>()
        alarmManager?.canScheduleExactAlarms() ?: false
    } else {
        true
    }
}


fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}


fun hasDndPermission(context: Context): Boolean {
    val notificationManager = context.getSystemService<NotificationManager>()
    return notificationManager?.isNotificationPolicyAccessGranted ?: false
}



fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safelyStartActivity(context, intent) { openAppSettings(context) }
    } else {
        openAppSettings(context)
    }
}


fun openDndSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    safelyStartActivity(context, intent) { openAppSettings(context) }
}


fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:${context.packageName}".toUri()
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    safelyStartActivity(context, intent)
}


fun openIgnoreBatteryOptimizationSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        "package:${context.packageName}".toUri()
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    safelyStartActivity(context, intent) { openAppSettings(context) }
}


private inline fun safelyStartActivity(
    context: Context,
    intent: Intent,
    onFallback: () -> Unit = {}
) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onFallback()
    }
}