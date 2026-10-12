/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.additional

import com.chisacwnu.hicwnu.data.model.JvmBasePreferences
import java.util.Locale


actual object PlatformLocaleManager : JvmBasePreferences("locale_settings.properties") {

    private const val PREF_KEY_LANGUAGE = "app_language_tag"

    actual fun setLanguageTag(tag: String) {
        if (tag.isEmpty()) {
            putString(PREF_KEY_LANGUAGE, null)
            Locale.setDefault(Locale.getDefault())
        } else {
            putString(PREF_KEY_LANGUAGE, tag)
            Locale.setDefault(Locale.forLanguageTag(tag))
        }
    }

    actual fun getCurrentLanguageTag(): String {
        return getString(PREF_KEY_LANGUAGE, "")
    }
}