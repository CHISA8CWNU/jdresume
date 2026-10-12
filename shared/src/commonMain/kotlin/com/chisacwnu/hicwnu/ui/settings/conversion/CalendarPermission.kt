/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.conversion

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCalendarPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit
