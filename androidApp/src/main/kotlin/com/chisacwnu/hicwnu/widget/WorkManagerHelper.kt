/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.widget

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


object WorkManagerHelper {

    private const val UI_UPDATE_WORK_NAME = "WidgetUiUpdateWorker_Periodic"
    private const val FULL_DATA_SYNC_WORK_NAME = "FullDataSyncWorker_Periodic"

    fun schedulePeriodicWork(context: Context) {
        Log.d("WidgetWorkManager", "正在调度小组件定期任务...")

                val uiUpdateWorkRequest = PeriodicWorkRequestBuilder<WidgetUiUpdateWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UI_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            uiUpdateWorkRequest
        )

                val fullDataSyncWorkRequest = PeriodicWorkRequestBuilder<FullDataSyncWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FULL_DATA_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            fullDataSyncWorkRequest
        )
    }

    fun cancelAllWork(context: Context) {
        Log.d("WidgetWorkManager", "正在取消所有小组件定期任务...")
        WorkManager.getInstance(context).cancelUniqueWork(UI_UPDATE_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(FULL_DATA_SYNC_WORK_NAME)
    }
}