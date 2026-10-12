/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.chisacwnu.hicwnu.data.model.AppThemeMode

@Composable
actual fun rememberColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    customLightPrimary: Color,
    customDarkPrimary: Color
): ColorScheme {
    val seedColor = if (darkTheme) customDarkPrimary else customLightPrimary

    return rememberMaterialKolorScheme(
        darkTheme = darkTheme,
        seedColor = seedColor
    )
}

@Composable
actual fun SetupPlatformThemeEffects(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    themeMode: AppThemeMode
) {
        }

actual val supportsDynamicColor: Boolean = false