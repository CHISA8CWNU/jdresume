/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.di

import okio.Path


interface AppStorage {
    val filesDir: Path
    val cacheDir: Path
    fun getDatabasePath(dbName: String): String
}