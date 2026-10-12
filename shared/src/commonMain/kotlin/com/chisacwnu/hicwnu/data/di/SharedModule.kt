/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.di

import okio.FileSystem
import okio.Path
import okio.SYSTEM
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(includes = [
    DatabaseModule::class,
    DataStoreModule::class
])
@ComponentScan("com.chisacwnu.hicwnu")
class SharedModule {

    @Single
    fun provideFileSystem(): FileSystem = FileSystem.SYSTEM

    @Single
    @Named("FilesDir")
    fun provideFilesDir(appStorage: AppStorage): Path {
        return appStorage.filesDir
    }

    
    @Single
    @Named("CacheDir")
    fun provideCacheDir(appStorage: AppStorage): Path {
        return appStorage.cacheDir
    }

    @Single
    @Named("UpdateDownloadDir")
    fun provideUpdateDownloadDir(appStorage: AppStorage): Path {
        return appStorage.filesDir / "updates"
    }
}