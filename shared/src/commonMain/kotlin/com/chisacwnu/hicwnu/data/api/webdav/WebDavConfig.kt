/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.api.webdav


data class WebDavConfig(
    val baseUrl: String,
    val username: String,
    val password: SenderPassword,
    val rootPath: String = "HiCWNU"
) {
    
    fun getCleanRootPath(): String {
        val trimmed = rootPath.trim().trim('/')
        return if (trimmed.isEmpty()) "" else "$trimmed/"
    }
}

typealias SenderPassword = String