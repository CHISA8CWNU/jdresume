/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.update

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ForceUpdateGate(viewModel: AppUpdateViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (!state.showDialog) return

    ForceUpdateDialog(
        state = state,
        onUpdate = { viewModel.onUpdateClicked() },
        onInstall = { viewModel.onInstallClicked() },
        onRetry = { viewModel.onRetryClicked() },
        onExitApp = { viewModel.onExitAppClicked() },
        onDismiss = { viewModel.onDismissRequested() }
    )
}