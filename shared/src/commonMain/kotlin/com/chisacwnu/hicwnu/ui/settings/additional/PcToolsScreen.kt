/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.tool.copyToClipboard
import com.chisacwnu.hicwnu.ui.components.ToastManager
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.action_cancel
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.chevron_right_24px
import hicwnu.shared.generated.resources.contact_gitee
import hicwnu.shared.generated.resources.contact_gitee_url
import hicwnu.shared.generated.resources.contact_qid
import hicwnu.shared.generated.resources.contact_qid_value
import hicwnu.shared.generated.resources.contact_wechat
import hicwnu.shared.generated.resources.contact_wechat_value
import hicwnu.shared.generated.resources.dialog_title_contact_author
import hicwnu.shared.generated.resources.download_24px
import hicwnu.shared.generated.resources.pc_tool_7zip_desc
import hicwnu.shared.generated.resources.pc_tool_7zip_title
import hicwnu.shared.generated.resources.pc_tool_contact_desc
import hicwnu.shared.generated.resources.pc_tool_contact_title
import hicwnu.shared.generated.resources.pc_tool_geek_desc
import hicwnu.shared.generated.resources.pc_tool_geek_title
import hicwnu.shared.generated.resources.pc_tool_office_desc
import hicwnu.shared.generated.resources.pc_tool_office_title
import hicwnu.shared.generated.resources.pc_tool_pdf24_desc
import hicwnu.shared.generated.resources.pc_tool_pdf24_title
import hicwnu.shared.generated.resources.pc_tool_win11_desc
import hicwnu.shared.generated.resources.pc_tool_win11_title
import hicwnu.shared.generated.resources.person_24px
import hicwnu.shared.generated.resources.title_pc_tools
import hicwnu.shared.generated.resources.toast_copied

private const val URL_OFFICE_GUIDE = "https://gitee.com/cwnu6/read/raw/master/1.pdf"
private const val URL_PDF24_INSTALLER = "https://gitee.com/cwnu6/read/raw/master/2.exe"
private const val URL_SEVEN_ZIP = "https://gitee.com/cwnu6/read/raw/master/5.exe"
private const val URL_GEEK_UNINSTALLER = "https://gitee.com/cwnu6/read/raw/master/3.exe"
private const val URL_WIN11_SETTINGS = "https://gitee.com/cwnu6/read/raw/master/4.zip"

private data class PcToolEntry(
    val title: String,
    val desc: String,
    val url: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcToolsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    var showContactDialog by remember { mutableStateOf(false) }

    val tools = listOf(
        PcToolEntry(
            title = stringResource(Res.string.pc_tool_office_title),
            desc = stringResource(Res.string.pc_tool_office_desc),
            url = URL_OFFICE_GUIDE
        ),
        PcToolEntry(
            title = stringResource(Res.string.pc_tool_pdf24_title),
            desc = stringResource(Res.string.pc_tool_pdf24_desc),
            url = URL_PDF24_INSTALLER
        ),
        PcToolEntry(
            title = stringResource(Res.string.pc_tool_7zip_title),
            desc = stringResource(Res.string.pc_tool_7zip_desc),
            url = URL_SEVEN_ZIP
        ),
        PcToolEntry(
            title = stringResource(Res.string.pc_tool_geek_title),
            desc = stringResource(Res.string.pc_tool_geek_desc),
            url = URL_GEEK_UNINSTALLER
        ),
        PcToolEntry(
            title = stringResource(Res.string.pc_tool_win11_title),
            desc = stringResource(Res.string.pc_tool_win11_desc),
            url = URL_WIN11_SETTINGS
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.title_pc_tools)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    tools.forEachIndexed { index, tool ->
                        PcToolRow(
                            icon = Res.drawable.download_24px,
                            title = tool.title,
                            desc = tool.desc,
                            onClick = { tool.url?.let { uriHandler.openUri(it) } }
                        )
                        if (index != tools.lastIndex) {
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                    PcToolRow(
                        icon = Res.drawable.person_24px,
                        title = stringResource(Res.string.pc_tool_contact_title),
                        desc = stringResource(Res.string.pc_tool_contact_desc),
                        onClick = { showContactDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    ContactAuthorDialog(
        showDialog = showContactDialog,
        onDismiss = { showContactDialog = false }
    )
}

@Composable
private fun PcToolRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = vectorResource(Res.drawable.chevron_right_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactAuthorDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    val uriHandler = LocalUriHandler.current
    val copiedText = stringResource(Res.string.toast_copied)

    val giteeUrl = stringResource(Res.string.contact_gitee_url)
    val wechatId = stringResource(Res.string.contact_wechat_value)
    val qqId = stringResource(Res.string.contact_qid_value)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.dialog_title_contact_author)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ContactLine(
                    text = stringResource(Res.string.contact_wechat),
                    onClick = {
                        copyToClipboard(wechatId)
                        ToastManager.show(copiedText)
                    }
                )
                ContactLine(
                    text = stringResource(Res.string.contact_qid),
                    onClick = {
                        copyToClipboard(qqId)
                        ToastManager.show(copiedText)
                    }
                )
                ContactLine(
                    text = stringResource(Res.string.contact_gitee),
                    onClick = { uriHandler.openUri(giteeUrl) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ContactLine(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    )
}
