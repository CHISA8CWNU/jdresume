/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.quickactions

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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.Destination
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.chevron_right_24px
import hicwnu.shared.generated.resources.desc_schedule_tweak
import hicwnu.shared.generated.resources.item_quick_actions
import hicwnu.shared.generated.resources.item_quick_delete
import hicwnu.shared.generated.resources.item_schedule_tweak
import hicwnu.shared.generated.resources.quick_delete_subtitle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(Res.string.item_quick_actions)) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

                        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                                        QuickActionItem(
                        title = stringResource(Res.string.item_schedule_tweak),
                        subtitle = stringResource(Res.string.desc_schedule_tweak),
                        onClick = { onNavigate(Destination.TweakSchedule) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                        QuickActionItem(
                        title = stringResource(Res.string.item_quick_delete),
                        subtitle = stringResource(Res.string.quick_delete_subtitle),
                        onClick = { onNavigate(Destination.QuickDelete) }
                    )
                }
            }
        }
    }
}


@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = vectorResource(Res.drawable.chevron_right_24px),
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}