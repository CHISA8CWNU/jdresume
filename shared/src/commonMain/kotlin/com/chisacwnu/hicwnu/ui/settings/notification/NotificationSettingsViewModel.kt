/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.model.AutoControlMode
import com.chisacwnu.hicwnu.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel


sealed interface NotificationDialogType {
    data object None : NotificationDialogType
    data object EditRemindMinutes : NotificationDialogType
    data object AutoModeSelection : NotificationDialogType
}


data class NotificationSettingsUiState(
    val reminderEnabled: Boolean = false,
    val remindBeforeMinutes: Int = 15,
    val isLoading: Boolean = false,
    val exactAlarmStatus: Boolean = false,
    val dndPermissionStatus: Boolean = false,
    val autoModeEnabled: Boolean = false,
    val autoControlMode: AutoControlMode = AutoControlMode.DND,
    val compatWearableSync: Boolean = false,
    val activeDialog: NotificationDialogType = NotificationDialogType.None
)


@KoinViewModel
class NotificationSettingsViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    
    private fun loadSettings() {
        viewModelScope.launch {
            appSettingsRepository.getAppSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    reminderEnabled = settings.reminderEnabled,
                    remindBeforeMinutes = settings.remindBeforeMinutes,
                    autoModeEnabled = settings.autoModeEnabled,
                    autoControlMode = settings.autoControlMode,
                    compatWearableSync = settings.compatWearableSync
                )
            }
        }
    }

    
    fun showDialog(type: NotificationDialogType) {
        _uiState.value = _uiState.value.copy(activeDialog = type)
    }

    
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(activeDialog = NotificationDialogType.None)
    }

    
    fun updateExactAlarmStatus(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(exactAlarmStatus = hasPermission)
    }

    
    fun updateDndPermissionStatus(hasPermission: Boolean) {
        _uiState.value = _uiState.value.copy(dndPermissionStatus = hasPermission)
    }

    
    fun updateReminderEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = appSettingsRepository.getAppSettings().first()
            appSettingsRepository.insertOrUpdateAppSettings(currentSettings.copy(reminderEnabled = isEnabled))
        }
    }

    
    fun updateCompatWearableSync(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = appSettingsRepository.getAppSettings().first()
            appSettingsRepository.insertOrUpdateAppSettings(currentSettings.copy(compatWearableSync = isEnabled))
        }
    }

    
    fun updateRemindBeforeMinutes(minutes: Int) {
        viewModelScope.launch {
            val currentSettings = appSettingsRepository.getAppSettings().first()
            appSettingsRepository.insertOrUpdateAppSettings(currentSettings.copy(remindBeforeMinutes = minutes))
            dismissDialog()
        }
    }

    
    fun updateAutoMode(isEnabled: Boolean, newControlMode: AutoControlMode) {
        viewModelScope.launch {
            val currentSettings = appSettingsRepository.getAppSettings().first()
            appSettingsRepository.insertOrUpdateAppSettings(
                currentSettings.copy(
                    autoModeEnabled = isEnabled,
                    autoControlMode = newControlMode
                )
            )
            dismissDialog()
        }
    }
}