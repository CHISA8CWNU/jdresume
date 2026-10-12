/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import school_index.AdapterCategory
import school_index.School


data class CategoryLastSchool(
    val id: String = "",
    val name: String = "",
    val resourceFolder: String = ""
) {
    
    val isEmpty: Boolean get() = id.isBlank()

    
    fun toSchool(): School {
        return School(
            id = id,
            name = name,
            resource_folder = resourceFolder
        )
    }
}


data class SchoolHistoryModel(
    val bachelor: CategoryLastSchool = CategoryLastSchool(),
    val postgraduate: CategoryLastSchool = CategoryLastSchool(),
    val general: CategoryLastSchool = CategoryLastSchool()
) {
    companion object {
        
                private val KEY_BACHELOR_ID = stringPreferencesKey("last_school_bachelor_id")
        private val KEY_BACHELOR_NAME = stringPreferencesKey("last_school_bachelor_name")
        private val KEY_BACHELOR_FOLDER = stringPreferencesKey("last_school_bachelor_folder")

                private val KEY_POSTGRAD_ID = stringPreferencesKey("last_school_postgrad_id")
        private val KEY_POSTGRAD_NAME = stringPreferencesKey("last_school_postgrad_name")
        private val KEY_POSTGRAD_FOLDER = stringPreferencesKey("last_school_postgrad_folder")

                private val KEY_GENERAL_ID = stringPreferencesKey("last_school_general_id")
        private val KEY_GENERAL_NAME = stringPreferencesKey("last_school_general_name")
        private val KEY_GENERAL_FOLDER = stringPreferencesKey("last_school_general_folder")

        
        fun fromPreferences(prefs: Preferences): SchoolHistoryModel {
            return SchoolHistoryModel(
                bachelor = CategoryLastSchool(
                    id = prefs[KEY_BACHELOR_ID] ?: "",
                    name = prefs[KEY_BACHELOR_NAME] ?: "",
                    resourceFolder = prefs[KEY_BACHELOR_FOLDER] ?: ""
                ),
                postgraduate = CategoryLastSchool(
                    id = prefs[KEY_POSTGRAD_ID] ?: "",
                    name = prefs[KEY_POSTGRAD_NAME] ?: "",
                    resourceFolder = prefs[KEY_POSTGRAD_FOLDER] ?: ""
                ),
                general = CategoryLastSchool(
                    id = prefs[KEY_GENERAL_ID] ?: "",
                    name = prefs[KEY_GENERAL_NAME] ?: "",
                    resourceFolder = prefs[KEY_GENERAL_FOLDER] ?: ""
                )
            )
        }

        
        fun getKeysForCategory(category: AdapterCategory): Triple<Preferences.Key<String>, Preferences.Key<String>, Preferences.Key<String>> {
            return when (category) {
                AdapterCategory.BACHELOR_AND_ASSOCIATE ->
                    Triple(KEY_BACHELOR_ID, KEY_BACHELOR_NAME, KEY_BACHELOR_FOLDER)

                AdapterCategory.POSTGRADUATE ->
                    Triple(KEY_POSTGRAD_ID, KEY_POSTGRAD_NAME, KEY_POSTGRAD_FOLDER)

                AdapterCategory.GENERAL_TOOL ->
                    Triple(KEY_GENERAL_ID, KEY_GENERAL_NAME, KEY_GENERAL_FOLDER)

                                else -> Triple(KEY_BACHELOR_ID, KEY_BACHELOR_NAME, KEY_BACHELOR_FOLDER)
            }
        }
    }
}