/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu
import com.chisacwnu.hicwnu.ui.settings.dorm.DormLightsOutScreen
import com.chisacwnu.hicwnu.ui.settings.dorm.DormWaterScreen

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.chisacwnu.hicwnu.data.model.StartScreen
import com.chisacwnu.hicwnu.ui.components.QqGroupNoticeDialog
import com.chisacwnu.hicwnu.ui.schedule.WeeklyScheduleScreen
import com.chisacwnu.hicwnu.ui.schedule.WeeklyScheduleViewModel
import com.chisacwnu.hicwnu.ui.schoolselection.list.AdapterSelectionScreen
import com.chisacwnu.hicwnu.ui.schoolselection.list.SchoolSelectionListScreen
import com.chisacwnu.hicwnu.ui.schoolselection.web.WebViewScreen
import com.chisacwnu.hicwnu.ui.settings.SettingsScreen
import com.chisacwnu.hicwnu.ui.settings.SettingsViewModel
import com.chisacwnu.hicwnu.ui.settings.about.AboutScreen
import com.chisacwnu.hicwnu.ui.settings.about.PrivacyPolicyScreen
import com.chisacwnu.hicwnu.ui.settings.about.ServiceTermsScreen
import com.chisacwnu.hicwnu.ui.settings.additional.LanguageSettingScreen
import com.chisacwnu.hicwnu.ui.settings.additional.MoreOptionsScreen
import com.chisacwnu.hicwnu.ui.settings.additional.PcToolsScreen
import com.chisacwnu.hicwnu.ui.settings.additional.QuickSettingsScreen
import com.chisacwnu.hicwnu.ui.settings.backup.BackupScreen
import com.chisacwnu.hicwnu.ui.settings.contribution.ContributionScreen
import com.chisacwnu.hicwnu.ui.settings.conversion.CourseTableConversionScreen
import com.chisacwnu.hicwnu.ui.settings.course.AddEditCourseScreen
import com.chisacwnu.hicwnu.ui.settings.coursemanagement.CourseInstanceListScreen
import com.chisacwnu.hicwnu.ui.settings.coursemanagement.CourseNameListScreen
import com.chisacwnu.hicwnu.ui.settings.coursetables.ManageCourseTablesScreen
import com.chisacwnu.hicwnu.ui.settings.notification.NotificationSettingsScreen
import com.chisacwnu.hicwnu.ui.settings.quickactions.QuickActionsScreen
import com.chisacwnu.hicwnu.ui.settings.quickactions.delete.QuickDeleteScreen
import com.chisacwnu.hicwnu.ui.settings.quickactions.tweaks.TweakScheduleScreen
import com.chisacwnu.hicwnu.ui.settings.style.StyleSettingsScreen
import com.chisacwnu.hicwnu.ui.settings.themesettings.ThemeSettingsScreen
import com.chisacwnu.hicwnu.ui.settings.time.TimeSlotManagementScreen
import com.chisacwnu.hicwnu.ui.theme.HiCwnuTheme
import com.chisacwnu.hicwnu.ui.today.TodayScheduleScreen
import com.chisacwnu.hicwnu.ui.today.TodayScheduleViewModel
import com.chisacwnu.hicwnu.ui.update.AppUpdateViewModel
import com.chisacwnu.hicwnu.ui.update.ForceUpdateGate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    if (state.isReady) {
        HiCwnuTheme(settings = state.appSettings) {
            val startDest = remember(state.appSettings.startScreen) {
                when (state.appSettings.startScreen) {
                    StartScreen.COURSE_SCHEDULE -> Destination.CourseSchedule
                    StartScreen.TODAY_SCHEDULE -> Destination.TodaySchedule
                }
            }
                        val openConversionAtStart = !state.appSettings.firstLaunchCompleted
            LaunchedEffect(openConversionAtStart) {
                if (openConversionAtStart) viewModel.onFirstLaunchCompleted()
            }
                        LaunchedEffect(Unit) {
                viewModel.onForceCourseConfigDefaults()
            }

            val updateViewModel: AppUpdateViewModel = koinViewModel()

            Box(modifier = Modifier.fillMaxSize()) {
                AppNavigation(
                    startDestination = startDest,
                    openConversionAtStart = openConversionAtStart,
                    updateViewModel = updateViewModel
                )
                if (!state.appSettings.qqGroupNoticeShown) {
                    QqGroupNoticeDialog(onClosed = viewModel::onQqGroupNoticeClosed)
                }
                ForceUpdateGate(updateViewModel)
            }
        }
    } else {
        Surface(modifier = Modifier.fillMaxSize()) {}
    }
}

@Composable
fun AppNavigation(
    startDestination: Destination,
    openConversionAtStart: Boolean = false,
    updateViewModel: AppUpdateViewModel
) {
    val backStack = rememberNavBackStack(
        configuration = navSavedStateConfig,
        startDestination
    )

        LaunchedEffect(backStack) {
        if (openConversionAtStart &&
            backStack.size == 1 &&
            backStack.lastOrNull() !is Destination.LanguageSettings
        ) {
            backStack.add(Destination.LanguageSettings(onboarding = true))
        }
    }

    val onNavigate: (Destination) -> Unit = remember(backStack) {
        { dest ->
            if (dest.isMainScreen) {
                if (backStack.lastOrNull() != dest) {
                    backStack.clear()
                    backStack.add(dest)
                }
            } else {
                if (backStack.lastOrNull() != dest) {
                                        if (dest.isOnboardingStep && (backStack.lastOrNull() as? Destination)?.isOnboardingStep == true) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                    backStack.add(dest)
                }
            }
        }
    }

    val onBack: () -> Unit = remember(backStack) {
        {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        }
    }

        val motionScheme = MaterialTheme.motionScheme
    val spatialSpec = motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = motionScheme.fastEffectsSpec<Float>()

    val weeklyViewModel: WeeklyScheduleViewModel = koinViewModel()
    val todayViewModel: TodayScheduleViewModel = koinViewModel()

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        transitionSpec = {
            val fromMain = initialState.metadata[HiCwnuNavMetadata.IsMainScreenKey] ?: false
            val toMain = targetState.metadata[HiCwnuNavMetadata.IsMainScreenKey] ?: false

            if (fromMain && toMain) {
                fadeIn(animationSpec = effectsSpec) togetherWith fadeOut(animationSpec = effectsSpec)
            } else {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = spatialSpec) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = spatialSpec) + fadeOut(animationSpec = effectsSpec)
            }
        },
        popTransitionSpec = {
            val fromMain = initialState.metadata[HiCwnuNavMetadata.IsMainScreenKey] ?: false
            val toMain = targetState.metadata[HiCwnuNavMetadata.IsMainScreenKey] ?: false

            if (fromMain && toMain) {
                fadeIn(animationSpec = effectsSpec) togetherWith fadeOut(animationSpec = effectsSpec)
            } else {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = spatialSpec) + fadeIn(animationSpec = effectsSpec) togetherWith
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = spatialSpec)
            }
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = spatialSpec) + fadeIn(animationSpec = effectsSpec) togetherWith
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = spatialSpec)
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        )
    ) { key ->
        val destination = key as Destination

        NavEntry(
            key = key,
            metadata = metadata {
                put(HiCwnuNavMetadata.IsMainScreenKey, destination.isMainScreen)
            }
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ScreenContent(
                    targetDest = destination,
                    onNavigate = onNavigate,
                    onBack = onBack,
                    weeklyViewModel = weeklyViewModel,
                    todayViewModel = todayViewModel,
                    updateViewModel = updateViewModel
                )
            }
        }
    }
}

@Composable
fun ScreenContent(
    targetDest: Destination,
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    weeklyViewModel: WeeklyScheduleViewModel,
    todayViewModel: TodayScheduleViewModel,
    updateViewModel: AppUpdateViewModel
) {
    when (targetDest) {
        Destination.CourseSchedule -> WeeklyScheduleScreen(onNavigate, onBack, viewModel = weeklyViewModel)
        Destination.Settings -> SettingsScreen(onNavigate, onBack, updateViewModel)
        Destination.TodaySchedule -> TodayScheduleScreen(onNavigate, onBack, viewModel = todayViewModel)
        Destination.TimeSlotSettings -> TimeSlotManagementScreen(onBack)
        Destination.ManageCourseTables -> ManageCourseTablesScreen(onBack)
        Destination.SchoolSelectionListScreen -> SchoolSelectionListScreen(onNavigate, onBack)
        is Destination.CourseTableConversion -> CourseTableConversionScreen(onNavigate, onBack, onboarding = targetDest.onboarding)
        Destination.NotificationSettings -> NotificationSettingsScreen(onBack)
        Destination.MoreOptions -> MoreOptionsScreen(onNavigate, onBack)
        Destination.PcTools -> PcToolsScreen(onBack)
        Destination.QuickActions -> QuickActionsScreen(onNavigate, onBack)
        Destination.TweakSchedule -> TweakScheduleScreen(onBack)
        Destination.ContributionList -> ContributionScreen(onBack)
        Destination.CourseManagementList -> CourseNameListScreen(onNavigate, onBack)
        Destination.StyleSettings -> StyleSettingsScreen(onBack)
        Destination.QuickDelete -> QuickDeleteScreen(onBack)
        is Destination.ThemeSettings -> ThemeSettingsScreen(onBack, onNavigate, targetDest.onboarding)
        is Destination.QuickSettings -> QuickSettingsScreen(onBack, onNavigate, targetDest.onboarding)
        Destination.BackupAndRestore -> BackupScreen(onBack)
        is Destination.LanguageSettings -> LanguageSettingScreen(onBack, onNavigate, targetDest.onboarding)
        Destination.About -> AboutScreen(onNavigate, onBack)
        Destination.PrivacyPolicy -> PrivacyPolicyScreen(onBack)
        Destination.ServiceTerms -> ServiceTermsScreen(onBack)
        Destination.DormLightsOut -> DormLightsOutScreen(onBack)
        Destination.DormWater -> DormWaterScreen(onBack)

        is Destination.AdapterSelection -> AdapterSelectionScreen(
            onNavigate, onBack, targetDest.schoolId, targetDest.schoolName, targetDest.categoryNumber, targetDest.resourceFolder
        )
        is Destination.WebView -> WebViewScreen(
            onNavigate, onBack, targetDest.initialUrl, targetDest.assetJsPath
        )
        is Destination.AddEditCourse -> AddEditCourseScreen(
            onBack, targetDest.courseId
        )
        is Destination.CourseManagementDetail -> CourseInstanceListScreen(
            targetDest.courseName, onBack, onNavigate
        )
    }
}