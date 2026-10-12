/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.conversion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberCalendarPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember { { currentOnResult(true) } }
}
