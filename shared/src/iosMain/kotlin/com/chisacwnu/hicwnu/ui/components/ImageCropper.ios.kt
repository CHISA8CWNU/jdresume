/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image


actual val isCropWindowEnabled: Boolean = true


actual suspend fun cropImageBitmapNative(
    source: ImageBitmap,
    srcLeft: Int,
    srcTop: Int,
    cropWidth: Int,
    cropHeight: Int
): ByteArray = withContext(Dispatchers.IO) {
    val skiaBitmap = source.asSkiaBitmap()

        val safeLeft = srcLeft.coerceIn(0, skiaBitmap.width - 1)
    val safeTop = srcTop.coerceIn(0, skiaBitmap.height - 1)
    val safeWidth = cropWidth.coerceAtLeast(1).coerceAtMost(skiaBitmap.width - safeLeft)
    val safeHeight = cropHeight.coerceAtLeast(1).coerceAtMost(skiaBitmap.height - safeTop)

        val destBitmap = Bitmap()
    val cropRect = IRect.makeXYWH(safeLeft, safeTop, safeWidth, safeHeight)
    skiaBitmap.extractSubset(destBitmap, cropRect)

        val image = Image.makeFromBitmap(destBitmap)
    val data = image.encodeToData(EncodedImageFormat.JPEG, 90)

    data?.bytes ?: throw IllegalStateException("iOS image cropping/encoding failed")
}