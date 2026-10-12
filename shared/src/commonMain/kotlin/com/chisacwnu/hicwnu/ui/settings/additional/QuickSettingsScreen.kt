/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.Destination
import com.chisacwnu.hicwnu.ui.settings.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.quick_settings_next
import hicwnu.shared.generated.resources.quick_settings_show_non_current_week
import hicwnu.shared.generated.resources.quick_settings_show_weekends
import hicwnu.shared.generated.resources.quick_settings_title


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsScreen(
    onBack: () -> Unit,
    onNavigate: (Destination) -> Unit,
    onboarding: Boolean = false
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.appSettings
    val showWeekends = uiState.courseConfig?.showWeekends ?: true

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(Res.string.quick_settings_title)) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
                        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.quick_settings_show_non_current_week),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = settings.showNonCurrentWeekCourses,
                    onCheckedChange = { viewModel.onShowNonCurrentWeekChanged(it) }
                )
            }

                        Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.quick_settings_show_weekends),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = showWeekends,
                    onCheckedChange = { viewModel.onShowWeekendsChanged(it) }
                )
            }

            Spacer(Modifier.height(32.dp))

                        Button(
                onClick = {
                    onNavigate(Destination.CourseTableConversion(onboarding = true))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.quick_settings_next))
            }
        }
    }
}