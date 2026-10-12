/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.widget.compact

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.chisacwnu.hicwnu.MainActivity
import com.chisacwnu.hicwnu.R
import com.chisacwnu.hicwnu.widget.WidgetSnapshot
import com.chisacwnu.hicwnu.widget.WidgetCourseProto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object CompactNativeRenderer {

    fun render(context: Context, snapshot: WidgetSnapshot): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_today_compact_native)

                resetWidgetState(rv)

                val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                val now = LocalTime.now()
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val allCourses = snapshot.courses
        val currentWeek = if (snapshot.current_week <= 0) null else snapshot.current_week

                val dateFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())
        rv.setTextViewText(R.id.tv_header_title, today.format(dateFormatter))

                if (currentWeek != null) {
            rv.setViewVisibility(R.id.tv_current_week, View.VISIBLE)
            rv.setTextViewText(R.id.tv_current_week, context.getString(R.string.status_current_week_format, currentWeek))
        } else {
            rv.setViewVisibility(R.id.tv_current_week, View.GONE)
        }

                if (currentWeek == null) {
            showStatus(rv, context, context.getString(R.string.title_vacation), context.getString(R.string.widget_vacation_expecting), isFullCover = true)
            return rv
        }

                val todayStr = today.toString()
        val tomorrowStr = tomorrow.toString()

        val todayRemaining = allCourses.filter {
            (it.date == todayStr || it.date.isBlank()) && !it.is_skipped && try { LocalTime.parse(it.end_time) > now } catch (e: Exception) { true }
        }.sortedBy { it.start_time }

        val tomorrowCourses = allCourses.filter { it.date == tomorrowStr && !it.is_skipped }.sortedBy { it.start_time }

                when {
            todayRemaining.isNotEmpty() -> {
                                renderCourseContent(context, rv, todayRemaining, snapshot, false)
            }
            tomorrowCourses.isNotEmpty() -> {
                                rv.setTextViewText(R.id.tv_header_title, context.getString(R.string.widget_tomorrow_course_preview))
                renderCourseContent(context, rv, tomorrowCourses, snapshot, true)
            }
            else -> {
                                val hasCoursesToday = allCourses.any { it.date == todayStr || it.date.isBlank() }
                val tip = if (!hasCoursesToday) {
                    context.getString(R.string.text_no_courses_today)
                } else {
                    context.getString(R.string.widget_today_courses_finished)
                }
                showStatus(rv, context, tip, "", isFullCover = false)
            }
        }

        return rv
    }

    
    private fun resetWidgetState(rv: RemoteViews) {
        rv.setViewVisibility(R.id.container_full_status, View.GONE)
        rv.setViewVisibility(R.id.inner_content_card, View.VISIBLE)
        rv.setViewVisibility(R.id.container_courses, View.GONE)
        rv.setViewVisibility(R.id.container_status, View.GONE)
        rv.setViewVisibility(R.id.tv_footer, View.GONE)
        rv.removeAllViews(R.id.container_courses)
    }

    
    private fun renderCourseContent(context: Context, rv: RemoteViews, courses: List<WidgetCourseProto>, snapshot: WidgetSnapshot, isTomorrow: Boolean) {
        rv.setViewVisibility(R.id.container_courses, View.VISIBLE)
        rv.setViewVisibility(R.id.container_status, View.GONE)
        rv.setViewVisibility(R.id.tv_footer, View.VISIBLE)

        courses.forEachIndexed { index, course ->
            val itemRv = RemoteViews(context.packageName, R.layout.widget_item_course_common)
            itemRv.setTextViewText(R.id.tv_course_name, course.name)
            itemRv.setTextViewText(R.id.tv_course_position, course.position)
            itemRv.setTextViewText(R.id.tv_course_time, "${course.start_time.take(5)}-${course.end_time.take(5)}")

            if (!(course.teacher.isBlank())) {
                itemRv.setViewVisibility(R.id.tv_course_teacher, View.VISIBLE)
                itemRv.setTextViewText(R.id.tv_course_teacher, course.teacher)
            } else {
                itemRv.setViewVisibility(R.id.tv_course_teacher, View.GONE)
            }

                        val style = snapshot.style
            val colorInt = course.color_int
            if (style != null && colorInt < style.course_color_maps.size) {
                val colorPair = style.course_color_maps[colorInt]
                itemRv.setInt(R.id.course_indicator, "setColorFilter",
                    colorPair.light_color.toInt()
                )
                itemRv.setInt(R.id.course_indicator_dark, "setColorFilter",
                    colorPair.dark_color.toInt()
                )
            }

            rv.addView(R.id.container_courses, itemRv)

            if (index < courses.size - 1) {
                rv.addView(R.id.container_courses, RemoteViews(context.packageName, R.layout.widget_divider_horizontal))
            }
        }

        val footerRes = if (isTomorrow) R.string.widget_course_total_count else R.string.widget_course_remaining_count
        rv.setTextViewText(R.id.tv_footer, context.getString(footerRes, courses.size))
    }

    private fun showStatus(rv: RemoteViews, context: Context, title: String, msg: String?, isFullCover: Boolean) {
        if (isFullCover) {
            rv.setViewVisibility(R.id.inner_content_card, View.GONE)
            rv.setViewVisibility(R.id.container_status, View.GONE)
            rv.setViewVisibility(R.id.container_full_status, View.VISIBLE)
            rv.setTextViewText(R.id.tv_full_status_title, title)
            if (!msg.isNullOrBlank()) {
                rv.setTextViewText(R.id.tv_full_status_msg, msg)
                rv.setViewVisibility(R.id.tv_full_status_msg, View.VISIBLE)
            }
        } else {
            rv.setViewVisibility(R.id.inner_content_card, View.VISIBLE)
            rv.setViewVisibility(R.id.container_courses, View.GONE)
            rv.setViewVisibility(R.id.tv_footer, View.GONE)
            rv.setViewVisibility(R.id.container_status, View.VISIBLE)
            rv.setViewVisibility(R.id.container_full_status, View.GONE)
            rv.setTextViewText(R.id.tv_status_title, title)
            if (!msg.isNullOrBlank()) {
                rv.setTextViewText(R.id.tv_status_msg, msg)
                rv.setViewVisibility(R.id.tv_status_msg, View.VISIBLE)
            } else {
                rv.setViewVisibility(R.id.tv_status_msg, View.GONE)
            }
        }
    }
}