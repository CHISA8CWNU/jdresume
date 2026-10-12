/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chisacwnu.hicwnu.data.sync.WidgetDataSynchronizer
import org.koin.android.annotation.KoinWorker


@KoinWorker
class WidgetUiUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("WidgetSync", "WidgetUiUpdateWorker 开始执行")
        updateAllWidgets(applicationContext)
        return Result.success()
    }
}


@KoinWorker
class FullDataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val widgetDataSynchronizer: WidgetDataSynchronizer
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("WidgetSync", "FullDataSyncWorker 开始执行")
        try {
            widgetDataSynchronizer.syncNow()
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}