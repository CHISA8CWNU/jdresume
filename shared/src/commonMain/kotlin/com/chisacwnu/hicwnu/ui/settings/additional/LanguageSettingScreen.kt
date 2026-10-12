/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chisacwnu.hicwnu.Destination
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.a11y_back
import hicwnu.shared.generated.resources.arrow_back_24px
import hicwnu.shared.generated.resources.item_language_settings
import hicwnu.shared.generated.resources.language_follow_system
import hicwnu.shared.generated.resources.language_names
import hicwnu.shared.generated.resources.language_tags


expect object PlatformLocaleManager {
    fun setLanguageTag(tag: String)
    fun getCurrentLanguageTag(): String
}


private data class LanguageItem(
    val name: String,
    val tag: String
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingScreen(
    onBack: () -> Unit,
    onNavigate: ((Destination) -> Unit)? = null,
    onboarding: Boolean = false
) {
        var currentTag by remember {
        mutableStateOf(PlatformLocaleManager.getCurrentLanguageTag())
    }

    var selectedInOnboarding by remember { mutableStateOf(!onboarding) }

        val tags = stringArrayResource(Res.array.language_tags)
    val names = stringArrayResource(Res.array.language_names)
    val followSystemText = stringResource(Res.string.language_follow_system)
    val scrollState = rememberScrollState()

    val languageList = remember(tags, names, followSystemText) {
        buildList {
            add(LanguageItem(followSystemText, ""))
            addAll(names.zip(tags) { name, tag -> LanguageItem(name, tag) })
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text(stringResource(Res.string.item_language_settings)) },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            languageList.forEach { item ->
                                val isSelected = if (!selectedInOnboarding) {
                    false
                } else if (item.tag.isEmpty()) {
                    currentTag.isEmpty()
                } else {
                    currentTag.startsWith(item.tag)
                }

                ListItem(
                    modifier = Modifier.clickable {
                        if (!isSelected) {
                            currentTag = item.tag
                                                        PlatformLocaleManager.setLanguageTag(item.tag)
                            if (onboarding) {
                                selectedInOnboarding = true
                                onNavigate?.invoke(Destination.ThemeSettings(onboarding = true))
                            }
                        }
                    },
                    headlineContent = { Text(text = item.name) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                    }
                )
            }
        }
    }
}