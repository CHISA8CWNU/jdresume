/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip
import okio.use
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import hicwnu.shared.generated.resources.Res


@Suppress("unused")
@Single(createdAtStart = true)
class ResourceInitializerManager(
    private val fileSystem: FileSystem,
    @Named("FilesDir") private val filesDir: Path,
    @Named("CacheDir") private val cacheDir: Path,
    @Named("AppVersionCode") private val appVersionCode: Int
) {
    private val targetRepoDir: Path = filesDir / "repo"
    private val versionStampFile: Path = filesDir / "repo_version_stamp"
    private val shareTempDir: Path = cacheDir / "share_temp"

    init {
        CoroutineScope(Dispatchers.IO).launch {
            initializeOfflineRepo()
            clearTempCaches()
        }
    }

    private fun readVersionStamp(): String? = try {
        if (fileSystem.exists(versionStampFile)) {
            fileSystem.read(versionStampFile) { readUtf8() }.trim()
        } else null
    } catch (e: Exception) { null }

    private fun writeVersionStamp() {
        try {
            fileSystem.write(versionStampFile) { writeUtf8(appVersionCode.toString()) }
        } catch (e: Exception) {  }
    }

    
    @OptIn(ExperimentalResourceApi::class)
    suspend fun initializeOfflineRepo(forceOverwrite: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val repoReady = fileSystem.exists(targetRepoDir / "index")
            val stampMatches = readVersionStamp() == appVersionCode.toString()

            if (!forceOverwrite && repoReady && stampMatches) {
                return@runCatching
            }

            val zipBytes = Res.readBytes("files/offline_schools.zip")
            val tempZipFile = filesDir / "temp_offline_schools.zip"

            fileSystem.write(tempZipFile) {
                write(zipBytes)
            }

            try {
                val zipFileSystem = fileSystem.openZip(tempZipFile)

                if (fileSystem.exists(targetRepoDir)) {
                    fileSystem.deleteRecursively(targetRepoDir)
                }
                fileSystem.createDirectories(targetRepoDir)

                unzipDirectory(zipFileSystem, "/".toPath(), targetRepoDir)
            } finally {
                fileSystem.delete(tempZipFile)
            }

            writeVersionStamp()
        }
    }

    
    suspend fun clearTempCaches(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (fileSystem.exists(shareTempDir)) {
                fileSystem.deleteRecursively(shareTempDir)
            }

            val tempSchoolsRepo = cacheDir / "temp_schools_repo"
            val tempIndexRepo = cacheDir / "temp_index_repo"
            if (fileSystem.exists(tempSchoolsRepo)) fileSystem.deleteRecursively(tempSchoolsRepo)
            if (fileSystem.exists(tempIndexRepo)) fileSystem.deleteRecursively(tempIndexRepo)
        }
    }

    
    private fun unzipDirectory(
        zipFileSystem: FileSystem,
        currentZipPath: Path,
        targetDir: Path
    ) {
        val entries = zipFileSystem.list(currentZipPath)
        for (entry in entries) {
            val destinationPath = targetDir / entry.name

            if (!destinationPath.toString().startsWith(targetDir.toString())) {
                throw IllegalArgumentException("Illegal zip path: ${entry.name}")
            }

            val metadata = zipFileSystem.metadata(entry)

            if (metadata.isDirectory) {
                fileSystem.createDirectories(destinationPath)
                unzipDirectory(zipFileSystem, entry, destinationPath)
            } else {
                destinationPath.parent?.let { fileSystem.createDirectories(it) }

                zipFileSystem.source(entry).use { source ->
                    fileSystem.sink(destinationPath).buffer().use { sink ->
                        sink.writeAll(source)
                    }
                }
            }
        }
    }
}