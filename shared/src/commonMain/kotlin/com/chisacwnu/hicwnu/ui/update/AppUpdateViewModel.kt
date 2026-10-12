/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.model.AppVersionInfo
import com.chisacwnu.hicwnu.data.repository.AppUpdateRepository
import com.chisacwnu.hicwnu.data.repository.DownloadProgress
import com.chisacwnu.hicwnu.data.repository.UpdateCheckResult
import com.chisacwnu.hicwnu.tool.AppUpdateCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

enum class UpdatePhase {
    Idle,
    Checking,
    UpToDate,
    Available,
    Downloading,
    DownloadFailed,
    ReadyToInstall,
    CheckFailed
}

data class AppUpdateUiState(
    val phase: UpdatePhase = UpdatePhase.Idle,
    val remoteInfo: AppVersionInfo? = null,
    val localVersionName: String = "",
    val downloadProgress: DownloadProgress = DownloadProgress.Idle,
    val showDialog: Boolean = false,
    val errorMessage: String = ""
)

@KoinViewModel
class AppUpdateViewModel(
    private val repository: AppUpdateRepository
) : ViewModel(), KoinComponent {

    private val coordinator: AppUpdateCoordinator? by lazy {
        runCatching { getKoin().get<AppUpdateCoordinator>() }.getOrNull()
    }

    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            repository.downloadProgress.collect { progress ->
                _uiState.update { it.copy(downloadProgress = progress) }
            }
        }
    }

    fun onManualCheckRequested() {
        if (_uiState.value.phase == UpdatePhase.Checking) return
        _uiState.update { it.copy(showDialog = true, phase = UpdatePhase.Idle) }
        check()
    }

    private fun check() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(phase = UpdatePhase.Checking, showDialog = true, errorMessage = "")
            }
            when (val result = repository.checkForUpdate()) {
                is UpdateCheckResult.Available -> _uiState.update {
                    it.copy(
                        phase = UpdatePhase.Available,
                        remoteInfo = result.info,
                        localVersionName = repository.currentVersionName
                    )
                }

                UpdateCheckResult.UpToDate -> _uiState.update {
                    it.copy(phase = UpdatePhase.UpToDate)
                }

                UpdateCheckResult.Failed -> _uiState.update {
                    it.copy(phase = UpdatePhase.CheckFailed, errorMessage = CHECK_FAILED_HINT)
                }
            }
        }
    }

    fun onUpdateClicked() {
        val info = _uiState.value.remoteInfo ?: return
        if (_uiState.value.phase == UpdatePhase.Downloading) return

        val coord = coordinator
        if (coord != null && !coord.canRequestPackageInstalls()) {

            coord.openInstallPermissionSettings()
        }

        viewModelScope.launch {
            _uiState.update { it.copy(phase = UpdatePhase.Downloading, errorMessage = "") }
            repository.resetProgress()
            val path = repository.downloadUpdate(info)
            _uiState.update {
                it.copy(
                    phase = if (path != null) UpdatePhase.ReadyToInstall else UpdatePhase.DownloadFailed,
                    errorMessage = if (path == null) DOWNLOAD_FAILED_HINT else ""
                )
            }
        }
    }

    fun onInstallClicked() {

        val path = (_uiState.value.downloadProgress as? DownloadProgress.Done)?.filePath
        if (path == null) {
            println("[AppUpdate] onInstallClicked: 拿不到已下载路径，progress=${_uiState.value.downloadProgress}")
            return
        }
        val coord = coordinator
        if (coord == null) {
            println("[AppUpdate] onInstallClicked: coordinator 为空（非 Android 平台或未注册）")
            return
        }
        val ok = coord.startInstall(path)
        println("[AppUpdate] onInstallClicked: path=$path startInstall=$ok")
        if (!ok) {

            coord.openInstallPermissionSettings()
        }
    }

    fun onRetryClicked() {
        val info = _uiState.value.remoteInfo
        if (info == null) {
            check()
        } else {
            onUpdateClicked()
        }
    }

    fun onDismissRequested() {
        _uiState.update {
            it.copy(
                showDialog = false,
                phase = UpdatePhase.Idle,
                remoteInfo = null,
                errorMessage = ""
            )
        }
        repository.resetProgress()
    }

    fun onExitAppClicked() {
        coordinator?.exitApp()
    }

    private companion object {
        const val CHECK_FAILED_HINT = "无法连接更新服务器，请检查网络后重试"
        const val DOWNLOAD_FAILED_HINT = "安装包下载失败，请检查网络后重试"
    }
}