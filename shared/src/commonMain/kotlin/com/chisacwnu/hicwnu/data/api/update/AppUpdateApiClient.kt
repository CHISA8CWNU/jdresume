/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.api.update

import com.chisacwnu.hicwnu.data.model.AppVersionInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AppUpdateApiClient(
    private val fileSystem: FileSystem,
    @Named("UpdateDownloadDir") private val downloadDir: Path
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val metaClient by lazy {
        HttpClient {
            expectSuccess = false
            install(HttpRedirect) {

                allowHttpsDowngrade = true
            }
            install(ContentNegotiation) {

                json(json)
            }
            defaultRequest {

                header(HttpHeaders.UserAgent, "HiCWNU-Schedule (Gitee-Dist)")

                header(HttpHeaders.CacheControl, "no-cache, no-store, max-age=0")
                header(HttpHeaders.Pragma, "no-cache")
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
        }
    }

    private val downloadClient by lazy {
        HttpClient {
            expectSuccess = false
            install(HttpRedirect) {
                allowHttpsDowngrade = true
            }
            defaultRequest {
                header(HttpHeaders.UserAgent, "HiCWNU-Schedule (Gitee-Dist)")
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 600_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    suspend fun fetchVersionInfo(versionJsonUrl: String): AppVersionInfo? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = metaClient.get(versionJsonUrl)
                if (response.status != HttpStatusCode.OK) {
                    println("[AppUpdate] version.json 返回非 200: ${response.status}")
                    return@runCatching null
                }
                val body = response.bodyAsText()
                val info = json.decodeFromString<AppVersionInfo>(body)
                println("[AppUpdate] 解析成功 versionCode=${info.versionCode} name=${info.versionName}")
                info
            }.onFailure { e ->

                println("[AppUpdate] 检查更新失败: ${e::class.simpleName} - ${e.message}")
            }.getOrNull()
        }

    suspend fun downloadApk(
        apkUrl: String,
        targetFileName: String,
        onProgress: (readBytes: Long, totalBytes: Long) -> Unit
    ): Path? = withContext(Dispatchers.IO) {
        runCatching {
            fileSystem.createDirectories(downloadDir)
            val target = downloadDir / targetFileName
            val part = downloadDir / "$targetFileName.part"

            if (fileSystem.exists(part)) fileSystem.delete(part)
            if (fileSystem.exists(target)) fileSystem.delete(target)

            val response = downloadClient.get(apkUrl)
            if (response.status != HttpStatusCode.OK) {
                return@runCatching null
            }

            val total = response.contentLength() ?: 0L
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(8192)
            var read = 0L

            fileSystem.write(part) {
                while (!channel.isClosedForRead) {
                    val n = channel.readAvailable(buffer)
                    if (n <= 0) break
                    write(buffer, 0, n)
                    read += n
                    onProgress(read, total)
                }
            }

            if (!isZipMagicValid(part)) {
                fileSystem.delete(part)
                return@runCatching null
            }

            fileSystem.atomicMove(part, target)
            onProgress(read, if (total > 0) total else read)
            target
        }.getOrElse {

            runCatching {
                val part = downloadDir / "$targetFileName.part"
                if (fileSystem.exists(part)) fileSystem.delete(part)
            }
            null
        }
    }

    suspend fun clearDownloadDir() = withContext(Dispatchers.IO) {
        runCatching {
            if (fileSystem.exists(downloadDir)) {
                fileSystem.listRecursively(downloadDir).forEach { p ->
                    if (fileSystem.metadata(p).isRegularFile) {
                        fileSystem.delete(p)
                    }
                }
            }
        }
        Unit
    }

    private suspend fun isZipMagicValid(path: Path): Boolean = runCatching {
        val head = fileSystem.read(path) { readByteArray(4) }
        head.size == 4 &&
            head[0] == 0x50.toByte() && head[1] == 0x4B.toByte() &&
            head[2] == 0x03.toByte() && head[3] == 0x04.toByte()
    }.getOrDefault(false)
}