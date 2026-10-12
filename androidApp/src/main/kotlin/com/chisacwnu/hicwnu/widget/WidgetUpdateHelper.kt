/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import com.chisacwnu.hicwnu.data.model.ScheduleGridStyle
import com.chisacwnu.hicwnu.data.model.schedule_style.ScheduleGridStyleProto
import com.chisacwnu.hicwnu.data.model.toProto
import com.chisacwnu.hicwnu.data.repository.WidgetRepository
import com.chisacwnu.hicwnu.widget.compact.CompactNativeProvider
import com.chisacwnu.hicwnu.widget.compact.CompactNativeRenderer
import com.chisacwnu.hicwnu.widget.double_days.DoubleDaysNativeProvider
import com.chisacwnu.hicwnu.widget.double_days.DoubleDaysNativeRenderer
import com.chisacwnu.hicwnu.widget.list_vertical.ListVerticalNativeProvider
import com.chisacwnu.hicwnu.widget.list_vertical.ListVerticalNativeRenderer
import com.chisacwnu.hicwnu.widget.tiny.TinyNativeProvider
import com.chisacwnu.hicwnu.widget.tiny.TinyNativeRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private object WidgetDependencyContainer : KoinComponent {
    val repository: WidgetRepository by inject()
    val styleDataStore: DataStore<ScheduleGridStyleProto> by inject()
}


suspend fun updateAllWidgets(context: Context) {
    try {
                val repository = WidgetDependencyContainer.repository
        val styleDataStore = WidgetDependencyContainer.styleDataStore

                val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        val dbCourses = withTimeoutOrNull(3.seconds) {
            repository.getWidgetCoursesByDateRange(today.toString(), tomorrow.toString()).first()
        } ?: emptyList()

        val currentWeek = withTimeoutOrNull(2.seconds) {
            repository.getCurrentWeekFlow().first()
        } ?: 0

        val currentStyle = withTimeoutOrNull(2.seconds) {
            styleDataStore.data.first()
        }

        val finalStyleToSync = if (currentStyle == null || currentStyle.course_color_maps.isEmpty()) {
            ScheduleGridStyle.DEFAULT.toProto()
        } else {
            currentStyle
        }

                val courseProtoList = dbCourses.map { course ->
            WidgetCourseProto(
                id = course.id,
                name = course.name,
                teacher = course.teacher,
                position = course.position,
                start_time = course.startTime,
                end_time = course.endTime,
                color_int = course.colorInt,
                is_skipped = course.isSkipped,
                date = course.date
            )
        }

        val snapshot = WidgetSnapshot(
            current_week = currentWeek,
            style = finalStyleToSync,
            courses = courseProtoList
        )

                val appWidgetManager = AppWidgetManager.getInstance(context)
        val nativeConfigs = listOf(
            TinyNativeProvider::class.java to TinyNativeRenderer::render,
            CompactNativeProvider::class.java to CompactNativeRenderer::render,
            DoubleDaysNativeProvider::class.java to DoubleDaysNativeRenderer::render,
            ListVerticalNativeProvider::class.java to ListVerticalNativeRenderer::render
        )

                nativeConfigs.forEachIndexed { index, (providerClass, renderFunc) ->
            val componentName = ComponentName(context, providerClass)
            val ids = appWidgetManager.getAppWidgetIds(componentName)

            if (ids.isNotEmpty()) {
                if (index > 0) {
                    delay(300.milliseconds)
                }

                try {
                    val remoteViews = renderFunc(context, snapshot)
                    appWidgetManager.updateAppWidget(componentName, remoteViews)
                    Log.d("WidgetUpdateHelper", "成功刷新规格 ${providerClass.simpleName}")
                } catch (e: Exception) {
                    Log.e("WidgetUpdateHelper", "规格 ${providerClass.simpleName} 渲染失败", e)
                }
            }
        }

    } catch (e: Exception) {
        Log.e("WidgetUpdateHelper", "更新流程异常: ${e.stackTraceToString()}")
    }
}