/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.conversion

import androidx.compose.foundation.background
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.Destination
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.a11y_details
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.chevron_right_24px
import hicwnu.shared.generated.resources.desc_school_import_quick
import hicwnu.shared.generated.resources.desc_sync_to_system_calendar
import hicwnu.shared.generated.resources.item_school_system_import
import hicwnu.shared.generated.resources.item_sync_to_system_calendar
import hicwnu.shared.generated.resources.title_conversion
import hicwnu.shared.generated.resources.toast_calendar_permission_denied


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseTableConversionScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    onboarding: Boolean = false,
    viewModel: CourseTableConversionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val calendarDeniedMessage = stringResource(Res.string.toast_calendar_permission_denied)

    val requestCalendarPermission = rememberCalendarPermissionLauncher { granted ->
        if (granted) {
            viewModel.onSyncToCalendarClick()
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar(calendarDeniedMessage) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ConversionEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ConversionEvent.ImportSucceeded -> {
                    if (onboarding) {
                        onNavigate(Destination.CourseSchedule)
                    }
                }

                else -> Unit
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = { Text(stringResource(Res.string.title_conversion)) },
                    navigationIcon = {
                        if (!onboarding) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.arrow_back_24px),
                                    contentDescription = stringResource(Res.string.a11y_back)
                                )
                            }
                        }
                    }
                )
                if (uiState.isLoading) {
                    LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ConversionRow(
                        title = stringResource(Res.string.item_school_system_import),
                        desc = stringResource(Res.string.desc_school_import_quick),
                        onClick = {
                            onNavigate(
                                Destination.WebView(
                                    initialUrl = "https://authserver.cwnu.edu.cn/authserver/login?service=https%3A%2F%2Fehall.cwnu.edu.cn%2Flogin%3FportalService%3Dhttps%253A%252F%252Fehall.cwnu.edu.cn%252Findex.html%2523%252F",
                                    assetJsPath = "CWNU/cwnu_01.js"
                                )
                            )
                        }
                    )
                    if (!onboarding) {
                        HorizontalDivider()
                        ConversionRow(
                            title = stringResource(Res.string.item_sync_to_system_calendar),
                            desc = stringResource(Res.string.desc_sync_to_system_calendar),
                            onClick = requestCalendarPermission
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))

            if (onboarding) {
                val boldClause = "（相当于输入两遍账密）"
                val hintParagraph1 = buildAnnotatedString {
                    append("手机若连接校内WiFi(CWNU_DIANXIN或CWNU_YIDONG)，则正常登录信息门户；若使用流量，则可能在登录时再出现一次登录页面，此时正常登录即可")
                    val start = length
                    append(boldClause)
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = length
                    )
                    append("。")
                }
                Text(
                    text = hintParagraph1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "若Wifi导入失败，则切换到流量状态；若流量状态导入失败，请切换Wifi。",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

}


@Composable
private fun ConversionRow(
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = vectorResource(Res.drawable.chevron_right_24px),
            contentDescription = stringResource(Res.string.a11y_details),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}