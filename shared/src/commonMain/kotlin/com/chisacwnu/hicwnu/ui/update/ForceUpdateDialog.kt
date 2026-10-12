/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.data.repository.DownloadProgress
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.update_action_confirm
import hicwnu.shared.generated.resources.update_action_exit_app
import hicwnu.shared.generated.resources.update_action_install
import hicwnu.shared.generated.resources.update_action_retry
import hicwnu.shared.generated.resources.update_action_update_now
import hicwnu.shared.generated.resources.update_already_latest
import hicwnu.shared.generated.resources.update_dialog_check_failed
import hicwnu.shared.generated.resources.update_dialog_checking
import hicwnu.shared.generated.resources.update_dialog_download_failed
import hicwnu.shared.generated.resources.update_dialog_downloading
import hicwnu.shared.generated.resources.update_dialog_found
import hicwnu.shared.generated.resources.update_dialog_latest_version
import hicwnu.shared.generated.resources.update_dialog_notes
import hicwnu.shared.generated.resources.update_dialog_ready_install
import hicwnu.shared.generated.resources.update_download_failed
import hicwnu.shared.generated.resources.update_downloading_percent
import hicwnu.shared.generated.resources.update_need_unknown_sources
import hicwnu.shared.generated.resources.update_title_check_update

@Composable
fun ForceUpdateDialog(
    state: AppUpdateUiState,
    onUpdate: () -> Unit,
    onInstall: () -> Unit,
    onRetry: () -> Unit,
    onExitApp: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(titleRes(state.phase)))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (state.phase) {
                    UpdatePhase.Checking -> {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(Res.string.update_dialog_checking),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    UpdatePhase.UpToDate -> {
                        Text(
                            text = stringResource(Res.string.update_already_latest),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    UpdatePhase.Available -> {
                        val info = state.remoteInfo
                        Text(
                            text = stringResource(
                                Res.string.update_dialog_latest_version,
                                info?.versionName.orEmpty()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (!info?.notes.isNullOrBlank()) {
                            Text(
                                text = stringResource(Res.string.update_dialog_notes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Text(
                                text = info?.notes.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    UpdatePhase.Downloading -> {
                        val progress = state.downloadProgress
                        val percent = (progress as? DownloadProgress.Running)?.percent ?: 0
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        Text(
                            text = stringResource(Res.string.update_downloading_percent, percent),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    UpdatePhase.DownloadFailed -> {
                        Text(
                            text = stringResource(Res.string.update_download_failed, state.errorMessage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(Res.string.update_need_unknown_sources),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    UpdatePhase.ReadyToInstall -> {
                        Text(
                            text = stringResource(Res.string.update_dialog_ready_install),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    UpdatePhase.CheckFailed -> {
                        Text(
                            text = stringResource(Res.string.update_dialog_check_failed, state.errorMessage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    UpdatePhase.Idle -> Unit
                }
            }
        },
        confirmButton = {
            when (state.phase) {
                UpdatePhase.Available -> {
                    TextButton(onClick = onUpdate) {
                        Text(stringResource(Res.string.update_action_update_now))
                    }
                }

                UpdatePhase.Downloading -> {
                    TextButton(onClick = {}, enabled = false) {
                        Text(stringResource(Res.string.update_dialog_downloading))
                    }
                }

                UpdatePhase.DownloadFailed -> {

                    TextButton(onClick = onRetry) {
                        Text(stringResource(Res.string.update_action_retry))
                    }
                    TextButton(onClick = onExitApp) {
                        Text(stringResource(Res.string.update_action_exit_app))
                    }
                }

                UpdatePhase.ReadyToInstall -> {
                    TextButton(onClick = onInstall) {
                        Text(stringResource(Res.string.update_action_install))
                    }
                }

                UpdatePhase.CheckFailed -> {
                    TextButton(onClick = onRetry) {
                        Text(stringResource(Res.string.update_action_retry))
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.update_action_confirm))
                    }
                }

                UpdatePhase.UpToDate -> {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.update_action_confirm))
                    }
                }

                else -> Unit
            }
        }
    )
}

@Composable
private fun titleRes(phase: UpdatePhase) = when (phase) {
    UpdatePhase.Available -> Res.string.update_dialog_found
    UpdatePhase.Downloading -> Res.string.update_dialog_downloading
    UpdatePhase.DownloadFailed -> Res.string.update_dialog_download_failed
    UpdatePhase.ReadyToInstall -> Res.string.update_dialog_ready_install
    else -> Res.string.update_title_check_update
}
