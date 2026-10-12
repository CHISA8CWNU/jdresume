/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool


expect object ZipUtils {
    
    fun createZip(entries: Map<String, ByteArray>): ByteArray
}