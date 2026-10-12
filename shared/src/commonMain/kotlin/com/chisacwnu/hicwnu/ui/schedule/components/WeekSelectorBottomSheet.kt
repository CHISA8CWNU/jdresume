/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.title_select_week


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectorBottomSheet(
    totalWeeks: Int,
    currentWeek: Int?,
    selectedWeek: Int?,
    onWeekSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val gridState = rememberLazyGridState()

        LaunchedEffect(currentWeek) {
        if (currentWeek != null && currentWeek > 0) {
                        gridState.animateScrollToItem(currentWeek - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.title_select_week),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

                        LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                columns = GridCells.Adaptive(minSize = 60.dp),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(totalWeeks) { weekIndex ->
                    val weekNumber = weekIndex + 1
                    val isCurrentWeek = weekNumber == currentWeek
                    val isSelectedWeek = weekNumber == selectedWeek

                                        val backgroundColor = when {
                        isSelectedWeek -> MaterialTheme.colorScheme.primary
                        isCurrentWeek -> MaterialTheme.colorScheme.primaryContainer
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelectedWeek -> MaterialTheme.colorScheme.onPrimary
                        isCurrentWeek -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .clickable { onWeekSelected(weekNumber) }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$weekNumber",
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}