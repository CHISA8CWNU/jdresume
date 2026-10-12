/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.chisacwnu.hicwnu.data.model.AppSettingsModel
import com.chisacwnu.hicwnu.data.model.AppThemeMode


val LocalIsDarkTheme = staticCompositionLocalOf { false }


@Composable
fun HiCwnuTheme(
    settings: AppSettingsModel,
    content: @Composable () -> Unit
) {
    val darkTheme = when (settings.themeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        HiCwnuTheme(
            darkTheme = darkTheme,
            dynamicColor = settings.useDynamicColor,
            customLightPrimary = Color(settings.customLightPrimary),
            customDarkPrimary = Color(settings.customDarkPrimary),
            themeMode = settings.themeMode,
            content = content
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HiCwnuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customLightPrimary: Color = DefaultThemeColor,
    customDarkPrimary: Color = DefaultThemeColor,
    themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    content: @Composable () -> Unit
) {
    val colorScheme = rememberColorScheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        customLightPrimary = customLightPrimary,
        customDarkPrimary = customDarkPrimary
    )

        SetupPlatformThemeEffects(
        colorScheme = colorScheme,
        darkTheme = darkTheme,
        themeMode = themeMode
    )

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


@Composable
fun rememberMaterialKolorScheme(
    darkTheme: Boolean,
    seedColor: Color,
    style: PaletteStyle = PaletteStyle.TonalSpot
): ColorScheme {
    return rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = darkTheme,
        style = style
    )
}


@Composable
expect fun rememberColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    customLightPrimary: Color,
    customDarkPrimary: Color
): ColorScheme


@Composable
expect fun SetupPlatformThemeEffects(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    themeMode: AppThemeMode
)


expect val supportsDynamicColor: Boolean