/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.contribution

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chisacwnu.hicwnu.data.model.ContributionList
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back_to_previous
import hicwnu.shared.generated.resources.a11y_contributor_avatar
import hicwnu.shared.generated.resources.action_retry
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.label_github
import hicwnu.shared.generated.resources.tab_adapter_development
import hicwnu.shared.generated.resources.tab_app_development
import hicwnu.shared.generated.resources.text_loading_failed
import hicwnu.shared.generated.resources.text_no_contributors
import hicwnu.shared.generated.resources.title_contribution_list


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionScreen(
    onBack: () -> Unit,
    viewModel: ContributionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.title_contribution_list)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back_to_previous)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ContributionTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = viewModel::selectTab
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                when (val state = uiState) {
                    ContributionUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is ContributionUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ErrorMessage(
                                message = state.message,
                                onRetry = viewModel::loadContributions
                            )
                        }
                    }

                    is ContributionUiState.Success -> {
                        val listToShow = when (selectedTabIndex) {
                            0 -> state.data.jiaowuadapter
                            1 -> state.data.appDev
                            else -> emptyList()
                        }
                        ContributorListContent(
                            list = listToShow,
                            onContributorClick = { url ->
                                uriHandler.openUri(url)
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        stringResource(Res.string.tab_adapter_development),
        stringResource(Res.string.tab_app_development)
    )

    PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = if (selectedTabIndex == index) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    )
                }
            )
        }
    }
}


@Composable
fun ContributorListContent(
    list: List<ContributionList.Contributor>,
    onContributorClick: (String) -> Unit
) {
    if (list.isEmpty()) {
        Text(
            text = stringResource(Res.string.text_no_contributors),
            modifier = Modifier.padding(top = 16.dp)
        )
        return
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(list, key = { it.url }) { contributor ->
            ContributorCard(
                contributor = contributor,
                onContributorClick = onContributorClick
            )
        }
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun ContributorCard(
    contributor: ContributionList.Contributor,
    onContributorClick: (String) -> Unit
) {
    val a11yAvatar = stringResource(Res.string.a11y_contributor_avatar, contributor.name)
    val labelGithub = stringResource(Res.string.label_github)

    val avatarBytes by produceState<ByteArray?>(initialValue = null, contributor.avatar) {
        value = try {
            Res.readBytes("files/contributors_data/${contributor.avatar}")
        } catch (_: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onContributorClick(contributor.url) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarBytes,
                contentDescription = a11yAvatar,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = contributor.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = labelGithub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}


@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    val textLoadingFailed = stringResource(Res.string.text_loading_failed, message)
    val actionRetry = stringResource(Res.string.action_retry)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = textLoadingFailed,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = onRetry) {
            Text(actionRetry)
        }
    }
}