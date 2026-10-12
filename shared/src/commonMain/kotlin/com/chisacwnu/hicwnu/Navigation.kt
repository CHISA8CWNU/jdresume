/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass


object HiCwnuNavMetadata {
    object IsMainScreenKey : NavMetadataKey<Boolean>
}


@Serializable
sealed interface Destination : NavKey {

    
    sealed interface MainDestination : Destination

        @Serializable data object CourseSchedule : MainDestination
    @Serializable data object Settings : MainDestination
    @Serializable data object TodaySchedule : MainDestination

        @Serializable data object TimeSlotSettings : Destination
    @Serializable data object ManageCourseTables : Destination
    @Serializable data object SchoolSelectionListScreen : Destination
    @Serializable data class CourseTableConversion(val onboarding: Boolean = false) : Destination
    @Serializable data object NotificationSettings : Destination
    @Serializable data object MoreOptions : Destination
    @Serializable data object PcTools : Destination
    @Serializable data object DormLightsOut : Destination
    @Serializable data object DormWater : Destination
    @Serializable data object QuickActions : Destination
    @Serializable data object TweakSchedule : Destination
    @Serializable data object QuickDelete : Destination
    @Serializable data object ContributionList : Destination
    @Serializable data object CourseManagementList : Destination
    @Serializable data object StyleSettings : Destination
    @Serializable data class ThemeSettings(val onboarding: Boolean = false) : Destination
    @Serializable data class QuickSettings(val onboarding: Boolean = false) : Destination
    @Serializable data object BackupAndRestore : Destination
    @Serializable data class LanguageSettings(val onboarding: Boolean = false) : Destination
    @Serializable data object About : Destination
    @Serializable data object PrivacyPolicy : Destination
    @Serializable data object ServiceTerms : Destination

        @Serializable
    data class AdapterSelection(
        val schoolId: String,
        val schoolName: String,
        val categoryNumber: Int,
        val resourceFolder: String
    ) : Destination

    @Serializable
    data class WebView(
        val initialUrl: String? = "about:blank",
        val assetJsPath: String? = null
    ) : Destination

    @Serializable
    data class AddEditCourse(
        val courseId: String? = null
    ) : Destination

    @Serializable
    data class CourseManagementDetail(
        val courseName: String
    ) : Destination
}

val Destination.isMainScreen: Boolean
    get() = this is Destination.MainDestination


val Destination.isOnboardingStep: Boolean
    get() = when (this) {
        is Destination.LanguageSettings -> onboarding
        is Destination.ThemeSettings -> onboarding
        is Destination.QuickSettings -> onboarding
        is Destination.CourseTableConversion -> onboarding
        else -> false
    }


val navSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
                subclass(Destination.CourseSchedule::class)
        subclass(Destination.Settings::class)
        subclass(Destination.TodaySchedule::class)

                subclass(Destination.TimeSlotSettings::class)
        subclass(Destination.ManageCourseTables::class)
        subclass(Destination.SchoolSelectionListScreen::class)
        subclass(Destination.CourseTableConversion::class)
        subclass(Destination.NotificationSettings::class)
        subclass(Destination.MoreOptions::class)
        subclass(Destination.PcTools::class)
        subclass(Destination.QuickActions::class)
        subclass(Destination.TweakSchedule::class)
        subclass(Destination.QuickDelete::class)
        subclass(Destination.ContributionList::class)
        subclass(Destination.CourseManagementList::class)
        subclass(Destination.StyleSettings::class)
        subclass(Destination.ThemeSettings::class)
        subclass(Destination.QuickSettings::class)
        subclass(Destination.BackupAndRestore::class)
        subclass(Destination.LanguageSettings::class)
        subclass(Destination.About::class)
        subclass(Destination.PrivacyPolicy::class)
        subclass(Destination.ServiceTerms::class)
        subclass(Destination.DormLightsOut::class)
        subclass(Destination.DormWater::class)

                subclass(Destination.AdapterSelection::class)
        subclass(Destination.WebView::class)
        subclass(Destination.AddEditCourse::class)
        subclass(Destination.CourseManagementDetail::class)
    }
}


val navSavedStateConfig = SavedStateConfiguration {
    serializersModule = navSerializersModule
}