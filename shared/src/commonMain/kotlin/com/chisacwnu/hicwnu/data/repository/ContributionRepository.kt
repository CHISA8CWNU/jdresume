/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import com.chisacwnu.hicwnu.data.model.ContributionList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import hicwnu.shared.generated.resources.Res



@Single
class ContributionRepository {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    
    suspend fun getContributions(): ContributionList = withContext(Dispatchers.IO) {
        try {
                        val bytes = Res.readBytes("files/contributors_data/contributors.json")
            val jsonString = bytes.decodeToString()
            return@withContext json.decodeFromString<ContributionList>(jsonString)
        } catch (e: Exception) {
            throw Exception("无法加载或解析贡献者数据文件", e)
        }
    }
}