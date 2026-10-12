/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.widget.list_vertical

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.chisacwnu.hicwnu.widget.WorkManagerHelper
import com.chisacwnu.hicwnu.widget.updateAllWidgets
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class ListVerticalNativeProvider : AppWidgetProvider() {
    private val scope = MainScope()

    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            updateAllWidgets(context)
        }
    }

    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
                WorkManagerHelper.schedulePeriodicWork(context)
    }

    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManagerHelper.cancelAllWork(context)
    }
}