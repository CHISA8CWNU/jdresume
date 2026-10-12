/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.chisacwnu.hicwnu.data.model.SchoolHistoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use
import school_index.Adapter
import school_index.AdapterCategory
import school_index.School
import school_index.SchoolIndex
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class SchoolRepository(
    private val fileSystem: FileSystem,
    @Named("FilesDir") private val filesDir: Path
) {

        private val RELEVANT_MENU_CATEGORIES = setOf(
        AdapterCategory.BACHELOR_AND_ASSOCIATE,
        AdapterCategory.POSTGRADUATE,
        AdapterCategory.GENERAL_TOOL
    )

    
    private suspend fun loadIndex(): SchoolIndex? {
        return withContext(Dispatchers.IO) {
            val internalPath = filesDir / "repo/index/school_index.pb"

            if (!fileSystem.exists(internalPath)) {
                println("错误：Protobuf 索引文件未找到: $internalPath")
                return@withContext null
            }

            try {
                fileSystem.source(internalPath).use { source ->
                    return@withContext SchoolIndex.ADAPTER.decode(source.buffer())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    
    suspend fun getSchools(): List<School> {
        val index = loadIndex() ?: return emptyList()

                val filteredSchools = index.schools.filter { school ->
            school.adapters.any { adapter ->
                adapter.category in RELEVANT_MENU_CATEGORIES
            }
        }

                return filteredSchools.sortedBy { it.initial.uppercase() + it.name }
    }

    
    suspend fun getAdaptersForSchool(schoolId: String): List<Adapter> {
        return withContext(Dispatchers.IO) {
            val index = loadIndex()
            val school = index?.schools?.find { it.id == schoolId }
            return@withContext school?.adapters ?: emptyList()
        }
    }

    
}

@Single
class SchoolHistoryRepository(
    @Named("SchoolHistory") private val dataStore: DataStore<Preferences>
) {
    val historyFlow: Flow<SchoolHistoryModel> = dataStore.data.map { prefs ->
        SchoolHistoryModel.fromPreferences(prefs)
    }

    
    suspend fun saveLastSchool(category: AdapterCategory, school: School) {
        dataStore.edit { prefs ->
            val keys = SchoolHistoryModel.getKeysForCategory(category)
            prefs[keys.first] = school.id
            prefs[keys.second] = school.name
            prefs[keys.third] = school.resource_folder
        }
    }

    
    suspend fun clearHistory(category: AdapterCategory) {
        dataStore.edit { prefs ->
            val keys = SchoolHistoryModel.getKeysForCategory(category)
            prefs.remove(keys.first)
            prefs.remove(keys.second)
            prefs.remove(keys.third)
        }
    }
}