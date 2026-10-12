/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image


actual val isCropWindowEnabled: Boolean = false


actual suspend fun cropImageBitmapNative(
    source: ImageBitmap,
    srcLeft: Int,
    srcTop: Int,
    cropWidth: Int,
    cropHeight: Int
): ByteArray = withContext(Dispatchers.IO) {
    val skiaBitmap = source.asSkiaBitmap()

        val image = Image.makeFromBitmap(skiaBitmap)
    val data = image.encodeToData(EncodedImageFormat.JPEG, 90)

    data?.bytes ?: throw IllegalStateException("Desktop image encoding failed")
}