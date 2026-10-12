/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun copyToClipboard(text: String) {
    try {
        val context: Context = ClipboardContextHolder.context ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText("cwnu", text)
        clipboard.setPrimaryClip(clip)
    } catch (_: Exception) {}
}

object ClipboardContextHolder : KoinComponent {
    var context: Context? = null
}