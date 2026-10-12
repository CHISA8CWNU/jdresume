/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap


interface FileManager {
    
    fun pickImage()

    
    fun importFile(allowedExtensions: List<String> = emptyList())

    
    fun exportFile(defaultFileName: String, bytes: ByteArray)
}


data class FileManagerCallbacks(
    val onImagePicked: ((ImageBitmap?) -> Unit)? = null,
    val onFileImported: ((bytes: ByteArray?, fileName: String?) -> Unit)? = null,
    val onFileExported: ((success: Boolean) -> Unit)? = null
)


@Composable
expect fun rememberFileManager(callbacks: FileManagerCallbacks): FileManager