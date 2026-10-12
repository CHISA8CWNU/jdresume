/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioSerializer
import com.chisacwnu.hicwnu.data.model.DualColor
import com.chisacwnu.hicwnu.data.model.ScheduleGridStyle
import com.chisacwnu.hicwnu.data.model.schedule_style.BorderTypeProto
import com.chisacwnu.hicwnu.data.model.schedule_style.ScheduleGridStyleProto
import com.chisacwnu.hicwnu.data.model.schedule_style.ScheduleModeProto
import com.chisacwnu.hicwnu.data.model.toCompose
import com.chisacwnu.hicwnu.data.model.toProto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable
import okio.BufferedSink
import okio.BufferedSource
import org.koin.core.annotation.Single


const val SCHEDULE_STYLE_DATASTORE_FILE_NAME = "schedule_style_settings.pb"


object ScheduleStyleSerializer : OkioSerializer<ScheduleGridStyleProto> {
    override val defaultValue: ScheduleGridStyleProto
        get() = ScheduleGridStyleProto()

    override suspend fun readFrom(source: BufferedSource): ScheduleGridStyleProto {
        return try {
            ScheduleGridStyleProto.ADAPTER.decode(source)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: ScheduleGridStyleProto, sink: BufferedSink) {
        ScheduleGridStyleProto.ADAPTER.encode(sink, t)
    }
}


@Serializable
data class StyleBackupEnvelope(
    val backupTimestamp: Long,
    val appVersionCode: Int,
    val styleProtoBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StyleBackupEnvelope) return false

        if (backupTimestamp != other.backupTimestamp) return false
        if (appVersionCode != other.appVersionCode) return false
        if (!styleProtoBytes.contentEquals(other.styleProtoBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = backupTimestamp.hashCode()
        result = 31 * result + appVersionCode
        result = 31 * result + styleProtoBytes.contentHashCode()
        return result
    }
}


@Single
class StyleSettingsRepository(
    private val dataStore: DataStore<ScheduleGridStyleProto>
) {

    companion object {
        
        const val STYLE_SCHEMA_VERSION = 1
    }

    private val _styleUpdatedChannel = Channel<Unit>(Channel.CONFLATED)
    val styleUpdatedFlow: Flow<Unit> = _styleUpdatedChannel.receiveAsFlow()

    
    
    suspend fun exportRawStyleBytes(): ByteArray {
        val currentProto = dataStore.data.first()
        val exportProto = currentProto.copy(background_image_path = "")
        return ScheduleGridStyleProto.ADAPTER.encode(exportProto)
    }

    
    suspend fun restoreRawStyleBytes(bytes: ByteArray): Result<Unit> = runCatching {
        val currentLocalProto = dataStore.data.first()
        val localWallpaperPath = currentLocalProto.background_image_path

        val backupProto = ScheduleGridStyleProto.ADAPTER.decode(bytes)
        val finalProto = backupProto.copy(background_image_path = localWallpaperPath)

        dataStore.updateData { finalProto }
        _styleUpdatedChannel.trySend(Unit)
    }

    
    suspend fun getStyleOnce(): ScheduleGridStyle {
        return dataStore.data.map { it.toCompose() }.first()
    }

    
    val styleFlow: Flow<ScheduleGridStyle> = dataStore.data
        .map { proto -> proto.toCompose() }

    private suspend fun updateStyle(
        transform: (ScheduleGridStyleProto) -> ScheduleGridStyleProto
    ) {
        dataStore.updateData { currentProto ->
            transform(currentProto)
        }
        _styleUpdatedChannel.trySend(Unit)
    }

    
    
    suspend fun setTimeColumnWidth(widthDp: Float) = updateStyle {
        it.copy(time_column_width_dp = widthDp)
    }

    
    suspend fun setDayHeaderHeight(heightDp: Float) = updateStyle {
        it.copy(day_header_height_dp = heightDp)
    }

    
    suspend fun setSectionHeight(heightDp: Float) = updateStyle {
        it.copy(section_height_dp = heightDp)
    }

    
    suspend fun setCourseBlockCornerRadius(radiusDp: Float) = updateStyle {
        it.copy(course_block_corner_radius_dp = radiusDp)
    }

    
    suspend fun setCourseBlockOuterPadding(paddingDp: Float) = updateStyle {
        it.copy(course_block_outer_padding_dp = paddingDp)
    }

    
    suspend fun setCourseBlockInnerPadding(paddingDp: Float) = updateStyle {
        it.copy(course_block_inner_padding_dp = paddingDp)
    }

    
    suspend fun setCourseBlockAlpha(alpha: Float) = updateStyle {
        it.copy(course_block_alpha_float = alpha)
    }

    
    suspend fun setCourseColorMaps(maps: List<DualColor>) {
        updateStyle {
            it.copy(course_color_maps = maps.map { dc -> dc.toProto() })
        }
    }

    
    suspend fun resetAllStyleSettings() {
        dataStore.updateData {
            ScheduleGridStyleProto()
        }
        _styleUpdatedChannel.trySend(Unit)
    }

    
    suspend fun setHideSectionTime(hide: Boolean) = updateStyle {
        it.copy(hide_section_time = hide)
    }

    
    suspend fun setHideDateUnderDay(hide: Boolean) = updateStyle {
        it.copy(hide_date_under_day = hide)
    }

    
    suspend fun setHideGridLines(hide: Boolean) = updateStyle {
        it.copy(hide_grid_lines = hide)
    }

    
    suspend fun setShowStartTime(show: Boolean) = updateStyle {
        it.copy(show_start_time = show)
    }

    
    suspend fun setCourseBlockFontScale(scale: Float) = updateStyle {
        it.copy(course_block_font_scale = scale)
    }

    
    suspend fun setHideLocation(hide: Boolean) = updateStyle {
        it.copy(hide_location = hide)
    }

    
    suspend fun setHideTeacher(hide: Boolean) = updateStyle {
        it.copy(hide_teacher = hide)
    }

    
    suspend fun setRemoveLocationAt(remove: Boolean) = updateStyle {
        it.copy(remove_location_at = remove)
    }

    
    suspend fun setTextAlignCenterHorizontal(center: Boolean) = updateStyle {
        it.copy(text_align_center_horizontal = center)
    }

    
    suspend fun setTextAlignCenterVertical(center: Boolean) = updateStyle {
        it.copy(text_align_center_vertical = center)
    }

    
    suspend fun setBorderType(type: BorderTypeProto) = updateStyle {
        it.copy(border_type = type)
    }

    
    suspend fun setScheduleMode(mode: ScheduleModeProto) = updateStyle {
        it.copy(schedule_mode = mode)
    }

    
    suspend fun setPageTextColor(color: Color?) = updateStyle {
        it.copy(page_text_color_long = color?.toArgb()?.toLong())
    }

    
    suspend fun setCourseTextColor(color: Color?) = updateStyle {
        it.copy(course_text_color_long = color?.toArgb()?.toLong())
    }

    
    suspend fun setBackgroundImagePath(path: String) = updateStyle {
        it.copy(background_image_path = path)
    }

    
    suspend fun resetAllStyleSettingsExceptWallpaper() {
        dataStore.updateData { currentProto ->
            val currentPath = currentProto.background_image_path
            ScheduleGridStyleProto().copy(background_image_path = currentPath)
        }
        _styleUpdatedChannel.trySend(Unit)
    }
}