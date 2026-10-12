/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.action_close
import hicwnu.shared.generated.resources.qq_group_notice_action
import hicwnu.shared.generated.resources.qq_group_notice_message
import hicwnu.shared.generated.resources.qq_group_notice_title

const val QQ_GROUP_JOIN_URL = "https://qm.qq.com/q/LtaeKtCZMc"

@Composable
fun QqGroupNoticeDialog(onClosed: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onClosed,
        title = { Text(stringResource(Res.string.qq_group_notice_title)) },
        text = { Text(stringResource(Res.string.qq_group_notice_message)) },
        confirmButton = {
            TextButton(onClick = {
                uriHandler.openUri(QQ_GROUP_JOIN_URL)
                onClosed()
            }) {
                Text(stringResource(Res.string.qq_group_notice_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onClosed) {
                Text(stringResource(Res.string.action_close))
            }
        }
    )
}
