/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.components

expect fun showPlatformToast(message: String)


object ToastManager {
    fun show(message: String) {
        if (message.isNotBlank()) {
            showPlatformToast(message)
        }
    }
}