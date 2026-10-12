/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.widget

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetAppSettingsDao {
    
    @Query("SELECT * FROM widget_semester_start_date WHERE id = 1")
    fun getAppSettings(): Flow<WidgetAppSettings?>

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: WidgetAppSettings)
}