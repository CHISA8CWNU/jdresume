/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.settings.conversion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chisacwnu.hicwnu.data.model.CourseImportExport
import com.chisacwnu.hicwnu.data.repository.CourseConversionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okio.BufferedSource
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.KoinViewModel
import hicwnu.shared.generated.resources.Res
import hicwnu.shared.generated.resources.*


@KoinViewModel
class CourseTableConversionViewModel(
    private val courseConversionRepository: CourseConversionRepository
) : ViewModel() {

        private val _uiState = MutableStateFlow(ConversionUiState())
    val uiState = _uiState.asStateFlow()

        private val _events = Channel<ConversionEvent>()
    val events = _events.receiveAsFlow()

    
    fun onImportClick() {
        _uiState.value = _uiState.value.copy(showImportTableDialog = true)
    }

    
    fun onExportClick() {
        _uiState.value = _uiState.value.copy(
            showExportTableDialog = true,
            exportType = ExportType.JSON
        )
    }

    
    fun onExportIcsClick() {
        _uiState.value = _uiState.value.copy(
            showExportTableDialog = true,
            exportType = ExportType.ICS
        )
    }

    
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showImportTableDialog = false,
            showExportTableDialog = false
        )
    }

    
    fun onImportTableSelected(tableId: String) {
        viewModelScope.launch {
            _events.send(ConversionEvent.LaunchImportFilePicker(tableId))
            dismissDialog()
        }
    }

    
    fun onExportTableSelected(tableId: String, alarmMinutes: Int?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (_uiState.value.exportType == ExportType.JSON) {
                    val jsonModel = courseConversionRepository.exportCourseTableToJson(tableId)
                    if (jsonModel != null) {
                        val jsonString = CourseImportExport.json.encodeToString(
                            CourseImportExport.CourseTableExportModel.serializer(),
                            jsonModel
                        )
                        _events.send(ConversionEvent.LaunchExportFileCreator(jsonString))
                    } else {
                        val message = getString(Res.string.error_export_table_not_found)
                        _events.send(ConversionEvent.ShowMessage(message))
                    }
                } else if (_uiState.value.exportType == ExportType.ICS) {
                    val icsContent = courseConversionRepository.exportToIcsString(tableId, alarmMinutes)
                    if (icsContent != null) {
                        _events.send(ConversionEvent.LaunchExportIcsFileCreator(icsContent))
                    } else {
                        val message = getString(Res.string.error_ics_export_data_failed)
                        _events.send(ConversionEvent.ShowMessage(message))
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: ""
                val message = getString(Res.string.error_export_failed, errorMessage)
                _events.send(ConversionEvent.ShowMessage(message))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
                dismissDialog()
            }
        }
    }

    
    fun handleFileImport(tableId: String, source: BufferedSource) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val jsonString = source.readUtf8()
                val importModel = CourseImportExport.json.decodeFromString<CourseImportExport.CourseTableImportModel>(jsonString)
                courseConversionRepository.importCourseTableFromJson(tableId, importModel)

                val message = getString(Res.string.toast_import_success)
                _events.send(ConversionEvent.ShowMessage(message))
                _events.send(ConversionEvent.ImportSucceeded)
            } catch (_: Exception) {
                val message = getString(Res.string.error_import_failed)
                _events.send(ConversionEvent.ShowMessage(message))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    
    fun onSyncToCalendarClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = courseConversionRepository.syncCurrentTableToSystemCalendar()
                val message = if (success) {
                    getString(Res.string.toast_sync_calendar_success)
                } else {
                    getString(Res.string.error_sync_calendar_failed)
                }
                _events.send(ConversionEvent.ShowMessage(message))
            } catch (_: Exception) {
                val message = getString(Res.string.error_sync_calendar_failed)
                _events.send(ConversionEvent.ShowMessage(message))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}


data class ConversionUiState(
    val isLoading: Boolean = false,
    val showImportTableDialog: Boolean = false,
    val showExportTableDialog: Boolean = false,
    val exportType: ExportType = ExportType.NONE
)


enum class ExportType {
    NONE,
    JSON,
    ICS
}


sealed class ConversionEvent {
    data class LaunchImportFilePicker(val tableId: String) : ConversionEvent()
    data class LaunchExportFileCreator(val jsonContent: String) : ConversionEvent()
    data class LaunchExportIcsFileCreator(val icsContent: String) : ConversionEvent()
    data class ShowMessage(val message: String) : ConversionEvent()
    data object ImportSucceeded : ConversionEvent()
}