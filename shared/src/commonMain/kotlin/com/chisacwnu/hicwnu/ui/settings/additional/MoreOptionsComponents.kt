/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.chevron_right_24px

@Composable
fun SettingListItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = { Text(text = title) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = trailingContent ?: {
            Icon(
                imageVector = vectorResource(Res.drawable.chevron_right_24px),
                contentDescription = null
            )
        }
    )
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            thickness = 0.5.dp
        )
    }
}
