/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schoolselection.web

import com.chisacwnu.hicwnu.data.model.CourseImportExport
import com.chisacwnu.hicwnu.data.repository.CourseConversionRepository
import com.chisacwnu.hicwnu.ui.components.ToastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class WebBridgeHandler(
    private val coroutineScope: CoroutineScope,
    private val uiEventChannel: SendChannel<WebUiEvent>,
    private val courseConversionRepository: CourseConversionRepository,
    private val onTaskCompleted: () -> Unit,
    private val evaluateJs: (script: String, callback: ((String?) -> Unit)?) -> Unit
) {
    private val json = CourseImportExport.json
    private var importTableId: String? = null

    
    fun setImportTableId(tableId: String?) {
        this.importTableId = tableId
    }

    
    fun onMessageReceived(jsonString: String) {
        try {
            val message = bridgeJson.decodeFromString<JsBridgeMessage>(jsonString)
            val callbackId = message.callbackId

            when (message.action) {
                "showToast" -> parsePayload<ShowToastPayload>(message.payload)?.let {
                    showToast(it.message)
                }

                "showAlert" -> parsePayload<ShowAlertPayload>(message.payload)?.let {
                    showAlert(it.titleText, it.contentText, it.confirmText, callbackId)
                }

                "showPrompt" -> parsePayload<ShowPromptPayload>(message.payload)?.let {
                    showPrompt(it.titleText, it.tipText, it.defaultText, it.validatorJsFunction, callbackId)
                }

                "showSingleSelection" -> parsePayload<ShowSingleSelectionPayload>(message.payload)?.let {
                    showSingleSelection(it.titleText, it.itemsJsonString, it.defaultSelectedIndex, callbackId)
                }

                "saveImportedCourses" -> parsePayload<SaveCoursesPayload>(message.payload)?.let {
                    saveImportedCourses(it.coursesJsonString, callbackId)
                }

                "saveCourseConfig" -> parsePayload<SaveConfigPayload>(message.payload)?.let {
                    saveCourseConfig(it.configJsonString, callbackId)
                }

                "savePresetTimeSlots" -> parsePayload<SaveTimeSlotsPayload>(message.payload)?.let {
                    savePresetTimeSlots(it.timeSlotsJsonString, callbackId)
                }

                "notifyTaskCompletion" -> notifyTaskCompletion()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    
    fun showToast(message: String) {
        ToastManager.show(message)
    }

    
    fun showAlert(
        titleText: String,
        contentText: String,
        confirmText: String? = null,
        callbackId: String? = null
    ) {
        val resolvedConfirmText = confirmText ?: "确定"
        val data = AlertDialogData(titleText, contentText, resolvedConfirmText)

        val promiseCallback: (Boolean) -> Unit = { confirmed ->
            if (callbackId != null) {
                resolveJsPromise(callbackId, if (confirmed) "true" else "false")
            }
        }

        val sendResult = uiEventChannel.trySend(WebUiEvent.ShowAlert(data, promiseCallback))
        if (sendResult.isFailure && callbackId != null) {
            rejectJsPromise(callbackId, "无法显示弹窗：事件队列已满")
        }
    }

    
    fun showPrompt(
        titleText: String,
        tipText: String,
        defaultText: String = "",
        validatorJsFunction: String? = null,
        callbackId: String? = null
    ) {
        val data = PromptDialogData(
            title = titleText,
            tip = tipText,
            defaultText = defaultText,
            validatorJsFunction = validatorJsFunction
        )

        val errorFlow = MutableSharedFlow<String?>(extraBufferCapacity = 1)

        val onCancel: () -> Unit = {
            if (callbackId != null) {
                resolveJsPromise(callbackId, "null")
            }
        }

        val onRequestValidation: (String, () -> Unit) -> Unit = { input, onSuccess ->
            val encodedInput = bridgeJson.encodeToString(input)

            if (data.validatorJsFunction.isNullOrEmpty()) {
                if (callbackId != null) {
                    resolveJsPromise(callbackId, encodedInput)
                }
                onSuccess()
            } else {
                val jsScript = "${data.validatorJsFunction}($encodedInput)"
                evaluateJs(jsScript) { result ->
                    val validationResult = result?.trim('\"')
                    if (validationResult.isNullOrEmpty() || validationResult.equals("false", ignoreCase = true)) {
                        if (callbackId != null) {
                            resolveJsPromise(callbackId, encodedInput)
                        }
                        onSuccess()
                    } else {
                        coroutineScope.launch {
                            errorFlow.emit(validationResult)
                        }
                    }
                }
            }
        }

        val sendResult = uiEventChannel.trySend(
            WebUiEvent.ShowPrompt(data, onRequestValidation, errorFlow.asSharedFlow(), onCancel)
        )
        if (sendResult.isFailure && callbackId != null) {
            rejectJsPromise(callbackId, "无法显示输入框：事件队列已满")
        }
    }

    fun showSingleSelection(
        titleText: String,
        itemsJsonString: String,
        defaultSelectedIndex: Int = -1,
        callbackId: String? = null
    ) {
        try {
            val items = json.decodeFromString<List<String>>(itemsJsonString)
            val data = SingleSelectionDialogData(titleText, items, defaultSelectedIndex)

            val promiseCallback: (Int?) -> Unit = { selectedIndex ->
                if (callbackId != null) {
                    resolveJsPromise(callbackId, selectedIndex?.toString() ?: "null")
                }
            }

            val sendResult = uiEventChannel.trySend(WebUiEvent.ShowSingleSelection(data, promiseCallback))
            if (sendResult.isFailure && callbackId != null) {
                rejectJsPromise(callbackId, "无法显示列表：事件队列已满")
            }
        } catch (e: Exception) {
            ToastManager.show("单选列表数据错误，无法显示。")
            if (callbackId != null) {
                rejectJsPromise(callbackId, "选项列表 JSON 无效: %s".replace("%s", e.message ?: ""))
            }
        }
    }

    fun saveImportedCourses(coursesJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val tableId = importTableId
            if (tableId == null) {
                coroutineScope.launch(Dispatchers.Main) {
                    ToastManager.show("导入失败：未选择课表。")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表选择已取消。")
                }
                return@launch
            }

            val result = runCatching {
                val importedCoursesList = json.decodeFromString<List<CourseImportExport.ImportCourseJsonModel>>(coursesJsonString)
                courseConversionRepository.importCoursesFromList(tableId, importedCoursesList)
            }

            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess {
                    ToastManager.show("课程导入成功！课表已更新。")
                    if (callbackId != null) resolveJsPromise(callbackId, "true")
                }.onFailure { e ->
                    ToastManager.show("课程导入失败: %s".replace("%s", e.message ?: ""))
                    if (callbackId != null) rejectJsPromise(callbackId, "课程导入失败: %s".replace("%s", e.message ?: ""))
                }
            }
        }
    }

    fun saveCourseConfig(configJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val tableId = importTableId
            if (tableId == null) {
                coroutineScope.launch(Dispatchers.Main) {
                    ToastManager.show("配置导入失败：未选择目标课表。")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表选择已取消或未设置。")
                }
                return@launch
            }

            val result = runCatching {
                val importedConfig = json.decodeFromString<CourseImportExport.CourseConfigJsonModel>(configJsonString)
                courseConversionRepository.importCourseConfig(tableId, importedConfig)
            }

            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess {
                    ToastManager.show("课表配置导入成功！")
                    if (callbackId != null) resolveJsPromise(callbackId, "true")
                }.onFailure { e ->
                    ToastManager.show("课表配置导入失败: %s".replace("%s", e.message ?: ""))
                    if (callbackId != null) rejectJsPromise(callbackId, "课表配置导入失败: %s".replace("%s", e.message ?: ""))
                }
            }
        }
    }

    fun savePresetTimeSlots(timeSlotsJsonString: String, callbackId: String? = null) {
        coroutineScope.launch(Dispatchers.Default) {
            val tableId = importTableId
            if (tableId == null) {
                coroutineScope.launch(Dispatchers.Main) {
                    ToastManager.show("导入失败：未选择课表。")
                    if (callbackId != null) rejectJsPromise(callbackId, "课表选择已取消。")
                }
                return@launch
            }

            val result = runCatching {
                val importedTimeSlotsJson = json.decodeFromString<List<CourseImportExport.TimeSlotJsonModel>>(timeSlotsJsonString)
                courseConversionRepository.importTimeSlots(tableId, importedTimeSlotsJson)
            }

            coroutineScope.launch(Dispatchers.Main) {
                result.onSuccess {
                    ToastManager.show("预设时间段导入成功！")
                    if (callbackId != null) resolveJsPromise(callbackId, "true")
                }.onFailure { e ->
                    ToastManager.show("预设时间段导入失败: %s".replace("%s", e.message ?: ""))
                    if (callbackId != null) rejectJsPromise(callbackId, "预设时间段导入失败: %s".replace("%s", e.message ?: ""))
                }
            }
        }
    }

    fun notifyTaskCompletion() {
        importTableId = null
        onTaskCompleted()
    }

    private inline fun <reified T> parsePayload(payloadJson: String?): T? {
        if (payloadJson == null) return null
        return try {
            bridgeJson.decodeFromString<T>(payloadJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resolveJsPromise(callbackId: String, resultRawJs: String) {
        val script = buildJsCallbackScript(callbackId, isSuccess = true, resultRawJs = resultRawJs)
        coroutineScope.launch(Dispatchers.Main) {
            evaluateJs(script, null)
        }
    }

    private fun rejectJsPromise(callbackId: String, errorText: String) {
        val safeErrorJson = bridgeJson.encodeToString(errorText)
        val script = buildJsCallbackScript(callbackId, isSuccess = false, resultRawJs = safeErrorJson)
        coroutineScope.launch(Dispatchers.Main) {
            evaluateJs(script, null)
        }
    }
}