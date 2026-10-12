/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.di


enum class OperatingSystem {
    WINDOWS,
    MACOS,
    LINUX,
    UNKNOWN;

    companion object {
        
        val current: OperatingSystem by lazy {
            val name = System.getProperty("os.name").lowercase()
            when {
                name.contains("win") -> WINDOWS
                name.contains("mac") || name.contains("darwin") -> MACOS
                name.contains("nux") || name.contains("nix") -> LINUX
                else -> UNKNOWN
            }
        }
    }
}