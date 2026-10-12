/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.item_privacy_policy
import hicwnu.shared.generated.resources.item_service_terms

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentScreen(
    title: String,
    doc: LegalDoc,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            doc.sections.forEach { section ->
                if (section.title != null) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                section.items.forEach { item ->
                    when (item) {
                        is LegalItem.Para -> Paragraph(text = item.text)
                        is LegalItem.Bullet -> Bullet(text = item.text)
                        is LegalItem.Component -> ComponentCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun Bullet(text: String) {
    Text(
        text = "•  $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

@Composable
private fun ComponentCard(component: LegalItem.Component) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = component.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "许可证：${component.license}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = component.purpose,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalDocumentScreen(
        title = stringResource(Res.string.item_privacy_policy),
        doc = LegalDocuments.privacyPolicy,
        onBack = onBack
    )
}

@Composable
fun ServiceTermsScreen(onBack: () -> Unit) {
    LegalDocumentScreen(
        title = stringResource(Res.string.item_service_terms),
        doc = LegalDocuments.serviceTerms,
        onBack = onBack
    )
}