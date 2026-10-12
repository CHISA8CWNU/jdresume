/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chisacwnu.hicwnu.Destination
import com.chisacwnu.hicwnu.ui.settings.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.app_name
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.home_24px
import hicwnu.shared.generated.resources.item_language_settings
import hicwnu.shared.generated.resources.item_start_screen_settings
import hicwnu.shared.generated.resources.label_version_prefix
import hicwnu.shared.generated.resources.language_24px
import hicwnu.shared.generated.resources.palette_24px
import hicwnu.shared.generated.resources.theme_settings_title
import hicwnu.shared.generated.resources.title_more_options

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val scrollState = rememberScrollState()

        val appVersionName: String = koinInject(named("AppVersionName"))

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        var showStartScreenDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.title_more_options)) },
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.label_version_prefix, appVersionName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

                        Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                                        SettingListItem(
                        icon = vectorResource(Res.drawable.language_24px),
                        title = stringResource(Res.string.item_language_settings),
                        onClick = { onNavigate(Destination.LanguageSettings()) }
                    )

                                        SettingListItem(
                        icon = vectorResource(Res.drawable.palette_24px),
                        title = stringResource(Res.string.theme_settings_title),
                        onClick = { onNavigate(Destination.ThemeSettings()) }
                    )

                                        SettingListItem(
                        icon = vectorResource(Res.drawable.home_24px),
                        title = stringResource(Res.string.item_start_screen_settings),
                        onClick = { showStartScreenDialog = true },
                        showDivider = false,
                        trailingContent = {
                            Text(
                                text = stringResource(uiState.appSettings.startScreen.labelRes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    
        StartScreenSelectionDialog(
        showDialog = showStartScreenDialog,
        currentSelected = uiState.appSettings.startScreen,
        onDismiss = { showStartScreenDialog = false },
        onConfirm = {
            viewModel.onStartScreenChanged(it)
            showStartScreenDialog = false
        }
    )
}
