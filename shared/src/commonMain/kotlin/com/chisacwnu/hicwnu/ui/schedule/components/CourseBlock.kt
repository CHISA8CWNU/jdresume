/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.chisacwnu.hicwnu.data.db.main.CourseWithWeeks
import com.chisacwnu.hicwnu.data.db.main.TimeSlot
import com.chisacwnu.hicwnu.data.model.schedule_style.BorderTypeProto
import com.chisacwnu.hicwnu.data.model.schedule_style.ScheduleModeProto
import com.chisacwnu.hicwnu.ui.theme.LocalIsDarkTheme

@Composable
fun CourseBlock(
    courseWrapper: CourseWithWeeks,
    isVisualDemoted: Boolean,
    style: ScheduleGridStyleComposed,
    timeSlots: List<TimeSlot>,
    modifier: Modifier = Modifier,
    isFloating: Boolean = false ) {
    val course = courseWrapper.course
    val isDarkTheme = LocalIsDarkTheme.current

        val colorIndex = course.colorInt.takeIf { it in style.courseColorMaps.indices }
    val courseColorAdapted: Color? = colorIndex?.let { index ->
        val baseColorMap = style.courseColorMaps[index]
        if (isDarkTheme) baseColorMap.dark else baseColorMap.light
    }
    val fallbackColorAdapted: Color = if (isDarkTheme) style.courseColorMaps.first().dark else style.courseColorMaps.first().light

    val currentAlpha = if (isFloating) 0.95f else style.courseBlockAlpha
    val blockColor = (courseColorAdapted ?: fallbackColorAdapted).copy(alpha = currentAlpha)
    val textColor = style.courseTextColor ?: MaterialTheme.colorScheme.onSurface

        val s13 = (13 * style.fontScale).sp
    val s10 = (10 * style.fontScale).sp

        val customStartTime = course.customStartTime
    val customEndTime = course.customEndTime
    val customTimeString = if (customStartTime != null && customEndTime != null) "$customStartTime - $customEndTime" else null
    val isCustomTimeCourse = customTimeString != null

    val timeTextToShow = if (style.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
                if (isCustomTimeCourse) {
            customTimeString
        } else {
            val startSlot = timeSlots.find { it.number == course.startSection }
            val endSlot = timeSlots.find { it.number == course.endSection }
            if (startSlot != null && endSlot != null) "${startSlot.startTime} - ${endSlot.endTime}" else null
        }
    } else {
                if (isCustomTimeCourse) {
            customTimeString
        } else if (style.showStartTime) {
            timeSlots.find { it.number == course.startSection }?.startTime
        } else {
            null
        }
    }

        val borderColor = if (isFloating) Color(0xFF2196F3) else MaterialTheme.colorScheme.outline
    val borderWidth = if (isFloating) 2.dp else 1.dp
    val borderAlpha = if (isFloating) 1.0f else style.courseBlockAlpha
    val shape = RoundedCornerShape(style.courseBlockCornerRadius)

    val borderModifier = when (style.borderType) {
        BorderTypeProto.BORDER_TYPE_SOLID -> {
            Modifier.border(borderWidth, borderColor.copy(alpha = borderAlpha), shape)
        }
        BorderTypeProto.BORDER_TYPE_DASHED -> {
            Modifier.drawBehind {
                val strokeWidth = borderWidth.toPx()
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = borderColor.copy(alpha = borderAlpha),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, pathEffect = dashPathEffect)
                )
            }
        }
        else -> {
            if (isFloating) Modifier.border(borderWidth, borderColor, shape) else Modifier
        }
    }

    val horizontalAlignment = if (style.textAlignCenterHorizontal) Alignment.CenterHorizontally else Alignment.Start
    val verticalArrangement = if (style.textAlignCenterVertical) Arrangement.Center else Arrangement.Top
    val textAlign = if (style.textAlignCenterHorizontal) TextAlign.Center else TextAlign.Start

        val floatingShadowModifier = if (isFloating) {
        Modifier.shadow(elevation = 8.dp, shape = shape, clip = false)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(floatingShadowModifier)
            .fillMaxSize()
            .then(borderModifier)
            .clip(shape)
            .background(color = blockColor)
    ) {
                Column(
            modifier = Modifier.fillMaxSize().padding(style.courseBlockInnerPadding),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement
        ) {
            if (timeTextToShow != null) {
                Text(
                    text = timeTextToShow,
                    fontSize = s10,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = textAlign,
                    style = TextStyle(lineHeight = 1.em)
                )
            }

            Text(
                text = course.name,
                fontSize = s13,
                fontWeight = FontWeight.Bold,
                color = textColor,
                overflow = TextOverflow.Ellipsis,
                textAlign = textAlign,
                modifier = Modifier.weight(1f, fill = false),
                style = TextStyle(lineHeight = 1.2.em)
            )

            if (!style.hideTeacher) {
                val teacher = course.teacher
                if (teacher.isNotBlank()) {
                    Text(text = teacher, fontSize = s10, color = textColor, textAlign = textAlign, overflow = TextOverflow.Ellipsis, style = TextStyle(lineHeight = 1.em))
                }
            }

            if (!style.hideLocation) {
                val position = course.position
                if (position.isNotBlank()) {
                    val prefix = if (style.removeLocationAt) "" else "@"
                    Text(text = "$prefix$position", fontSize = s10, color = textColor, textAlign = textAlign, overflow = TextOverflow.Ellipsis, style = TextStyle(lineHeight = 1.em))
                }
            }
        }

                if (isVisualDemoted && !isFloating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = (if (isDarkTheme) Color.Black else Color.White).copy(alpha = 0.618f))
                    .drawBehind {
                        val stripeWidth = 5.dp.toPx()
                        val stripeColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.06f)
                        val brush = Brush.linearGradient(
                            0.0f to stripeColor, 0.45f to stripeColor,
                            0.55f to Color.Transparent, 1.0f to Color.Transparent,
                            start = Offset(0f, 0f), end = Offset(stripeWidth, stripeWidth), tileMode = TileMode.Repeated
                        )
                        drawRect(brush = brush)
                    }
            )
        }
    }
}