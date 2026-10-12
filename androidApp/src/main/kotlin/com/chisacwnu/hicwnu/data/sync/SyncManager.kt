/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.sync

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.chisacwnu.hicwnu.data.repository.StyleSettingsRepository
import com.chisacwnu.hicwnu.service.CourseNotificationWorker
import com.chisacwnu.hicwnu.service.DndSchedulerWorker
import com.chisacwnu.hicwnu.widget.updateAllWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single


@Single(createdAtStart = true)
class SyncManager(
    private val appContext: Context,
    private val widgetDataSynchronizer: WidgetDataSynchronizer,
    private val styleSettingsRepository: StyleSettingsRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
                widgetDataSynchronizer.syncCompletedFlow
            .onEach {
                Log.d("SyncManager", "收到共享层同步完成通知，正在调度 Worker 任务及刷新小组件...")
                triggerNotificationWorker()
                DndSchedulerWorker.enqueueWork(appContext)
                updateAllWidgets(appContext)
            }
            .launchIn(scope)

                styleSettingsRepository.styleUpdatedFlow
            .onEach {
                Log.d("SyncManager", "收到样式更改通知，正在刷新小组件...")
                updateAllWidgets(appContext)
            }
            .launchIn(scope)

        Log.d("SyncManager", "Android 平台同步调度器初始化完毕。")
    }

    private fun triggerNotificationWorker() {
        val workRequest = OneTimeWorkRequestBuilder<CourseNotificationWorker>().build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "CourseNotificationWorker_Sync_Update",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}