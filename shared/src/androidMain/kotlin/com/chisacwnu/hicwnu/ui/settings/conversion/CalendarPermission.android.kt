/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.conversion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


@Composable
actual fun rememberCalendarPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
                currentOnResult(
            result[Manifest.permission.READ_CALENDAR] == true &&
                result[Manifest.permission.WRITE_CALENDAR] == true
        )
    }

    return remember(launcher) {
        {
            if (hasCalendarPermission(context)) {
                currentOnResult(true)
            } else {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                    )
                )
            }
        }
    }
}


private fun hasCalendarPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
        PackageManager.PERMISSION_GRANTED
}
