/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.coursemanagement

import androidx.compose.foundation.background
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.Destination
import com.chisacwnu.hicwnu.navigation.AddEditCourseChannel
import com.chisacwnu.hicwnu.navigation.PresetCourseData
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.a11y_cancel_selection
import hicwnu.shared.generated.resources.a11y_delete
import hicwnu.shared.generated.resources.a11y_enter_selection_mode
import hicwnu.shared.generated.resources.a11y_exit_selection_mode
import hicwnu.shared.generated.resources.action_add
import hicwnu.shared.generated.resources.action_deselect_all
import hicwnu.shared.generated.resources.action_select_all
import hicwnu.shared.generated.resources.add_24px
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.check_24px
import hicwnu.shared.generated.resources.close_24px
import hicwnu.shared.generated.resources.delete_24px
import hicwnu.shared.generated.resources.item_course_management
import hicwnu.shared.generated.resources.menu_open_24px
import hicwnu.shared.generated.resources.text_no_unique_courses_hint
import hicwnu.shared.generated.resources.title_selected_items_count


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseNameListScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: CourseNameListViewModel = koinViewModel()
) {
    val uniqueCourseNames by viewModel.uniqueCourseNames.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedCourseNames = remember { mutableStateListOf<String>() }

    val exitSelectionMode: () -> Unit = {
        isSelectionMode = false
        selectedCourseNames.clear()
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        if (isSelectionMode) {
                            stringResource(Res.string.title_selected_items_count, selectedCourseNames.size)
                        } else {
                            stringResource(Res.string.item_course_management)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                exitSelectionMode()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        val icon = if (isSelectionMode) vectorResource(Res.drawable.close_24px) else vectorResource(Res.drawable.arrow_back_24px)
                        val description = if (isSelectionMode) {
                            stringResource(Res.string.a11y_cancel_selection)
                        } else {
                            stringResource(Res.string.a11y_back)
                        }
                        Icon(icon, contentDescription = description)
                    }
                },
                actions = {
                    val totalCount = uniqueCourseNames.size
                    val selectedCount = selectedCourseNames.size
                    val isAllSelected = totalCount > 0 && selectedCount == totalCount

                                        if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                if (isAllSelected) {
                                                                        selectedCourseNames.clear()
                                } else {
                                                                        selectedCourseNames.clear()
                                    selectedCourseNames.addAll(uniqueCourseNames.map { it.name })
                                }
                            },
                            enabled = totalCount > 0
                        ) {
                            val selectAllStringRes = if (isAllSelected) Res.string.action_deselect_all else Res.string.action_select_all
                            Icon(vectorResource(Res.drawable.check_24px), contentDescription = stringResource(selectAllStringRes))
                        }
                    }

                                        if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                if (selectedCourseNames.isNotEmpty()) {
                                    coroutineScope.launch {
                                                                                viewModel.deleteSelectedCourses(selectedCourseNames)
                                                                                exitSelectionMode()
                                    }
                                }
                            },
                                                        enabled = selectedCourseNames.isNotEmpty()
                        ) {
                            Icon(vectorResource(Res.drawable.delete_24px), contentDescription = stringResource(Res.string.a11y_delete))
                        }
                    }

                                        IconButton(
                        onClick = {
                            if (isSelectionMode) {
                                                                exitSelectionMode()
                            } else {
                                                                isSelectionMode = true
                            }
                        }
                    ) {
                        val descriptionRes = if (isSelectionMode) {
                            Res.string.a11y_exit_selection_mode
                        } else {
                            Res.string.a11y_enter_selection_mode
                        }
                        Icon(vectorResource(Res.drawable.menu_open_24px), contentDescription = stringResource(descriptionRes))
                    }
                }
            )
        },
        floatingActionButton = {
                        if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                                                coroutineScope.launch {
                                                        val defaultPresetData = PresetCourseData(
                                startSection = 1,
                                endSection = 2
                            )

                                                        AddEditCourseChannel.sendEvent(defaultPresetData)

                            onNavigate(Destination.AddEditCourse(courseId = null))
                        }
                    }
                ) {
                    Icon(vectorResource(Res.drawable.add_24px), contentDescription = stringResource(Res.string.action_add))
                }
            }
        }
    ) { paddingValues ->
        if (uniqueCourseNames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(Res.string.text_no_unique_courses_hint), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
                        LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uniqueCourseNames, key = { it.name }) { item ->
                    val isSelected = item.name in selectedCourseNames

                    CourseNameCard(
                        name = item.name,
                        instanceCount = item.count,
                        isSelected = isSelected,
                        onCourseClick = { clickedName ->
                            if (isSelectionMode) {
                                if (selectedCourseNames.contains(clickedName)) {
                                    selectedCourseNames.remove(clickedName)
                                } else {
                                    selectedCourseNames.add(clickedName)
                                }
                            } else {
                                onNavigate(Destination.CourseManagementDetail(courseName = clickedName))
                            }
                        },
                        onCourseLongClick = { clickedName ->
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedCourseNames.add(clickedName)
                            }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CourseNameCard(
    name: String,
    instanceCount: Int,
    isSelected: Boolean,
    onCourseClick: (String) -> Unit,
    onCourseLongClick: (String) -> Unit
) {
    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Card(
        colors = cardColors,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .combinedClickable(
                onClick = { onCourseClick(name) },
                onLongClick = { onCourseLongClick(name) }
            )
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
                        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 32.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

                        Badge(
                content = { Text(instanceCount.toString(), style = MaterialTheme.typography.labelSmall) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
            )
        }
    }
}