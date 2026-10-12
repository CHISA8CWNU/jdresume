/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow



data class PresetCourseData(
        val name: String? = null,
    val teacher: String? = null,
    val position: String? = null,
    val remark: String? = null,             val day: Int? = null,
        val startSection: Int? = null,          val endSection: Int? = null,
        val isCustomTime: Boolean = false,      val customStartTime: String? = null,    val customEndTime: String? = null,
    val presetWeeks: Set<Int>? = null,      val colorIndex: Int? = null         )


object AddEditCourseChannel {
    private val channel = Channel<PresetCourseData>(Channel.CONFLATED)
    fun sendEvent(data: PresetCourseData) {
        channel.trySend(data)
    }
    val presetDataFlow = channel.receiveAsFlow()
}