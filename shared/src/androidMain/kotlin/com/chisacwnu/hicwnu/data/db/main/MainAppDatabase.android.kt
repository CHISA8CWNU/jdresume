/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.db.main

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.chisacwnu.hicwnu.data.di.AppStorage
import org.koin.mp.KoinPlatform

actual fun createMainDatabase(appStorage: AppStorage): MainAppDatabase {
    val context = KoinPlatform.getKoin().get<Context>()
    val dbPath = appStorage.getDatabasePath("main_app_database")
    return Room.databaseBuilder<MainAppDatabase>(
        context = context,
        name = dbPath,
        factory = { MainAppDatabaseConstructor.initialize() }
    )
        .addMigrations(*ALL_MIGRATIONS)
        .setDriver(AndroidSQLiteDriver())
        .build()
}