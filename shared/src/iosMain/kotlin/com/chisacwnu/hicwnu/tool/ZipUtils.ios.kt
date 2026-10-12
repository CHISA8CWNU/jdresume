/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool


actual object ZipUtils {
    actual fun createZip(entries: Map<String, ByteArray>): ByteArray {
        throw NotImplementedError("ZipUtils.createZip is not yet implemented for iOS platform.")
    }
}