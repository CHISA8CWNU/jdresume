/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schedule.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chisacwnu.hicwnu.data.model.DualColor
import com.chisacwnu.hicwnu.data.model.ScheduleGridStyle
import com.chisacwnu.hicwnu.data.model.schedule_style.BorderTypeProto
import com.chisacwnu.hicwnu.data.model.schedule_style.ScheduleModeProto



data class ScheduleGridStyleComposed(
        val timeColumnWidth: Dp,
    val dayHeaderHeight: Dp,
    val sectionHeight: Dp,

        val courseBlockCornerRadius: Dp,
    val courseBlockOuterPadding: Dp,
    val courseBlockInnerPadding: Dp,
    val courseBlockAlpha: Float,

        val backgroundImagePath: String,

        val fontScale: Float,

        val courseColorMaps: List<DualColor>,

        val hideGridLines: Boolean,          val hideSectionTime: Boolean,
    val hideDateUnderDay: Boolean,
    val showStartTime: Boolean,

    val hideLocation: Boolean,           val hideTeacher: Boolean,            val removeLocationAt: Boolean,
    val textAlignCenterHorizontal: Boolean,     val textAlignCenterVertical: Boolean,       val borderType: BorderTypeProto,            val scheduleMode: ScheduleModeProto,
    val pageTextColor: Color?,     val courseTextColor: Color?, ) {
    companion object {
        
        fun ScheduleGridStyle.toComposedStyle(): ScheduleGridStyleComposed {
            return ScheduleGridStyleComposed(
                timeColumnWidth = this.timeColumnWidthDp.dp,
                dayHeaderHeight = this.dayHeaderHeightDp.dp,
                sectionHeight = this.sectionHeightDp.dp,
                courseBlockCornerRadius = this.courseBlockCornerRadiusDp.dp,
                courseBlockOuterPadding = this.courseBlockOuterPaddingDp.dp,
                courseBlockInnerPadding = this.courseBlockInnerPaddingDp.dp,
                courseBlockAlpha = this.courseBlockAlphaFloat,
                fontScale = this.courseBlockFontScale,
                courseColorMaps = this.courseColorMaps.map { dual ->
                    DualColor(
                        light = Color(dual.light.toArgb()),
                        dark = Color(dual.dark.toArgb())
                    )
                },
                hideGridLines = this.hideGridLines,
                hideSectionTime = this.hideSectionTime,
                hideDateUnderDay = this.hideDateUnderDay,
                showStartTime = this.showStartTime,
                hideLocation = this.hideLocation,
                hideTeacher = this.hideTeacher,
                removeLocationAt = this.removeLocationAt,
                backgroundImagePath = this.backgroundImagePath ?: "",
                textAlignCenterHorizontal = this.textAlignCenterHorizontal,
                textAlignCenterVertical = this.textAlignCenterVertical,
                borderType = this.borderType,
                scheduleMode = this.scheduleMode,
                pageTextColor = this.pageTextColorLong?.let { Color(it) },
                courseTextColor = this.courseTextColorLong?.let { Color(it) },
            )
        }
    }
}