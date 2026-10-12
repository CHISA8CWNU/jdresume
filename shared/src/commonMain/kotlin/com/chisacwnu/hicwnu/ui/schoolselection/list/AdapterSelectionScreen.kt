/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schoolselection.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.Destination
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import school_index.Adapter
import school_index.AdapterCategory
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back_to_school_list
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.category_bachelor_associate
import hicwnu.shared.generated.resources.category_general_tool
import hicwnu.shared.generated.resources.category_other
import hicwnu.shared.generated.resources.category_postgraduate
import hicwnu.shared.generated.resources.info_24px
import hicwnu.shared.generated.resources.label_contributor_format
import hicwnu.shared.generated.resources.label_contributor_unknown
import hicwnu.shared.generated.resources.text_no_adapter_for_category_school
import hicwnu.shared.generated.resources.text_no_detailed_description


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdapterSelectionScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    schoolId: String,
    schoolName: String,
    categoryNumber: Int,
    resourceFolder: String,
    viewModel: SchoolSelectionViewModel = koinViewModel()
) {
        var adapters by remember { mutableStateOf<List<Adapter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

        val currentCategory = remember(categoryNumber) {
        AdapterCategory.fromValue(categoryNumber) ?: AdapterCategory.BACHELOR_AND_ASSOCIATE
    }

    @Composable
    fun getCategoryDisplayName(): String {
        return when (currentCategory) {
            AdapterCategory.BACHELOR_AND_ASSOCIATE -> stringResource(Res.string.category_bachelor_associate)
            AdapterCategory.POSTGRADUATE -> stringResource(Res.string.category_postgraduate)
            AdapterCategory.GENERAL_TOOL -> stringResource(Res.string.category_general_tool)
            else -> stringResource(Res.string.category_other)
        }
    }

        LaunchedEffect(schoolId, currentCategory) {
        isLoading = true
        try {
            viewModel.updateSelectedCategory(currentCategory)
            adapters = viewModel.getAdaptersForSchoolAndCategory(schoolId)
        } catch (e: Exception) {
            adapters = emptyList()
        } finally {
            isLoading = false
        }
    }

    val categoryDisplayName = getCategoryDisplayName()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$schoolName - $categoryDisplayName", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back_to_school_list)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                adapters.isEmpty() -> {
                    Text(
                        text = stringResource(Res.string.text_no_adapter_for_category_school, categoryDisplayName),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(adapters, key = { it.adapter_id }) { adapter ->
                            AdapterCard(
                                adapter = adapter,
                                onClick = { selectedAdapter ->
                                    val initialUrl = (selectedAdapter.import_url ?: "").ifBlank { "about:blank" }
                                    val jsFileName = selectedAdapter.asset_js_path

                                                                        val assetJsPath = if (jsFileName.isNotBlank()) {
                                        "$resourceFolder/$jsFileName"
                                    } else {
                                        "$resourceFolder/${selectedAdapter.adapter_id}.js"
                                    }
                                    onNavigate(
                                        Destination.WebView(
                                            initialUrl = initialUrl,
                                            assetJsPath = assetJsPath
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AdapterCard(
    adapter: Adapter,
    onClick: (Adapter) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick(adapter) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = adapter.adapter_name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

                        Text(
                text = adapter.description.ifBlank { stringResource(Res.string.text_no_detailed_description) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = vectorResource(Res.drawable.info_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        Res.string.label_contributor_format,
                        adapter.maintainer.ifBlank { stringResource(Res.string.label_contributor_unknown) }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}