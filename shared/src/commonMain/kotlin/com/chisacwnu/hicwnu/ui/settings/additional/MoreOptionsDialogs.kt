/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chisacwnu.hicwnu.data.model.StartScreen
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.action_cancel
import hicwnu.shared.generated.resources.dialog_select_start_screen


@Composable
fun StartScreenSelectionDialog(
    showDialog: Boolean,
    currentSelected: StartScreen,
    onDismiss: () -> Unit,
    onConfirm: (StartScreen) -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.dialog_select_start_screen)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                StartScreen.entries.forEach { screen ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirm(screen) },
                        headlineContent = { Text(stringResource(screen.labelRes)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            RadioButton(
                                selected = screen == currentSelected,
                                onClick = { onConfirm(screen) }
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}
