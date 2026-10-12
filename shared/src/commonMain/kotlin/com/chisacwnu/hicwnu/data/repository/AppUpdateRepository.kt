/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import com.chisacwnu.hicwnu.data.api.update.AppUpdateApiClient
import com.chisacwnu.hicwnu.data.model.AppVersionInfo
import com.chisacwnu.hicwnu.data.model.VERSION_JSON_BASE_URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Clock

sealed interface DownloadProgress {
    data object Idle : DownloadProgress
    data class Running(val readBytes: Long, val totalBytes: Long) : DownloadProgress {
        val percent: Int
            get() = if (totalBytes > 0) ((readBytes * 100) / totalBytes).toInt().coerceIn(0, 100) else 0
    }

    data class Done(val filePath: String) : DownloadProgress
    data class Failed(val message: String) : DownloadProgress
}

sealed interface UpdateCheckResult {
    data class Available(val info: AppVersionInfo) : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    data object Failed : UpdateCheckResult
}

@Single
class AppUpdateRepository(
    private val api: AppUpdateApiClient,
    @Named("AppVersionCode") private val localVersionCode: Int,
    @Named("AppVersionName") private val localVersionName: String
) {

    private val _downloadProgress = MutableStateFlow<DownloadProgress>(DownloadProgress.Idle)
    val downloadProgress: StateFlow<DownloadProgress> = _downloadProgress.asStateFlow()

    val currentVersionName: String = localVersionName

    suspend fun checkForUpdate(): UpdateCheckResult {
        val remote = runCatching {
            api.fetchVersionInfo(VERSION_JSON_BASE_URL)
        }.getOrNull() ?: return UpdateCheckResult.Failed

        if (!remote.isValid()) return UpdateCheckResult.Failed
        return if (remote.hasUpdateThan(localVersionCode)) {
            UpdateCheckResult.Available(remote)
        } else {
            UpdateCheckResult.UpToDate
        }
    }

    suspend fun downloadUpdate(info: AppVersionInfo): String? {
        _downloadProgress.value = DownloadProgress.Running(0L, info.apkSize)

        runCatching { api.clearDownloadDir() }
        val fileName = "update-${info.versionCode}.apk"
        val path = api.downloadApk(info.apkUrl, fileName) { read, total ->
            _downloadProgress.value = DownloadProgress.Running(read, total)
        }
        return if (path != null) {
            _downloadProgress.value = DownloadProgress.Done(path.toString())
            path.toString()
        } else {
            _downloadProgress.value = DownloadProgress.Failed("下载失败，请检查网络后重试")
            null
        }
    }

    fun resetProgress() {
        _downloadProgress.value = DownloadProgress.Idle
    }
}