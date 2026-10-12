/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schoolselection.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


expect val isDesktopPlatform: Boolean


@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)


interface WebViewController {
    val currentUrl: String
    fun reload()
    fun goBack(): Boolean
    fun canGoBack(): Boolean
    fun setDevToolsEnabled(enabled: Boolean)

    
    fun executeScript(jsCode: String)

    
    fun evaluateJavascript(script: String, callback: ((String?) -> Unit)? = null)
}


@Composable
expect fun rememberWebViewController(): WebViewController


@Composable
expect fun PlatformWebView(
    modifier: Modifier = Modifier,
    url: String,
    isDesktopMode: Boolean,
    isDevToolsEnabled: Boolean,
    controller: WebViewController,
    bridgeHandler: WebBridgeHandler,
    onProgressChange: (Float) -> Unit,
    onTitleChange: (String) -> Unit,
    onNavigateToSchedule: () -> Unit
)