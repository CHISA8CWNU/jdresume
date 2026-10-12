/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


actual val isCropWindowEnabled: Boolean = true


actual suspend fun cropImageBitmapNative(
    source: ImageBitmap,
    srcLeft: Int,
    srcTop: Int,
    cropWidth: Int,
    cropHeight: Int
): ByteArray = withContext(Dispatchers.Default) {
    val androidBitmap = source.asAndroidBitmap()

        val safeLeft = srcLeft.coerceIn(0, androidBitmap.width - 1)
    val safeTop = srcTop.coerceIn(0, androidBitmap.height - 1)
    val safeWidth = cropWidth.coerceAtLeast(1).coerceAtMost(androidBitmap.width - safeLeft)
    val safeHeight = cropHeight.coerceAtLeast(1).coerceAtMost(androidBitmap.height - safeTop)

        val croppedBitmap = Bitmap.createBitmap(
        androidBitmap,
        safeLeft,
        safeTop,
        safeWidth,
        safeHeight
    )

        ByteArrayOutputStream().use { stream ->
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.toByteArray()
    }
}