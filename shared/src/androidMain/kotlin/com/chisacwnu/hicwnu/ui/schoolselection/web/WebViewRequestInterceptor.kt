/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.ui.schoolselection.web

import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.parsing.ParseException
import io.ktor.http.parseHeaderValue
import io.ktor.http.takeFrom
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.util.Collections


class WebViewRequestInterceptor {
    companion object {
        private val ktorClientNoRedirects = HttpClient(CIO) {
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }

        private val ktorClientWithRedirects = HttpClient(CIO) {
            followRedirects = true
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }

        private val postBodyRegistry = Collections.synchronizedMap(mutableMapOf<String, RegisteredPostData>())

        fun registerPostData(id: String, body: String, contentType: String) {
            postBodyRegistry[id] = RegisteredPostData(body, contentType)
        }

        private data class RegisteredPostData(val body: String, val contentType: String)
    }

    private val cookieManager = CookieManager.getInstance()

    fun intercept(request: WebResourceRequest, isDesktopMode: Boolean): WebResourceResponse? {
        val rawUrl = request.url.toString()

                if (!rawUrl.startsWith("http") || !isDesktopMode) return null

        val requestIdHeader = request.requestHeaders["X-WebView-Post-Id"]
        val requestIdParam = request.url.getQueryParameter("_webview_post_id")
        val requestId = requestIdHeader ?: requestIdParam

                                if (!request.isForMainFrame && requestId == null) {
            return null
        }

                val url = if (requestIdParam != null) {
            val uriBuilder = request.url.buildUpon().clearQuery()
            request.url.queryParameterNames.forEach { name ->
                if (name != "_webview_post_id") {
                    request.url.getQueryParameters(name).forEach { value ->
                        uriBuilder.appendQueryParameter(name, value)
                    }
                }
            }
            uriBuilder.build().toString()
        } else {
            rawUrl
        }

        val registeredData = requestId?.let { postBodyRegistry.remove(it) }

                if (request.method.uppercase() != "GET" && registeredData == null) {
            return null
        }

        val client = if (request.isForMainFrame) ktorClientNoRedirects else ktorClientWithRedirects

        try {
            return runBlocking {
                val response = client.request {
                    this.url.takeFrom(url)
                    this.method = HttpMethod.parse(request.method)

                    if (registeredData != null) {
                        val rawContentType = registeredData.contentType.ifBlank { "application/x-www-form-urlencoded" }
                        val parsedContentType = try {
                            ContentType.parse(rawContentType)
                        } catch (e: Exception) {
                            ContentType.Application.FormUrlEncoded
                        }
                        setBody(ByteArrayContent(registeredData.body.toByteArray(Charsets.UTF_8), parsedContentType))
                    }

                    headers {
                        request.requestHeaders.forEach { (key, value) ->
                                                        if (!key.equals("X-Requested-With", ignoreCase = true) &&
                                !key.equals("X-WebView-Post-Id", ignoreCase = true)) {
                                append(key, value)
                            }
                        }

                        val cookies = cookieManager.getCookie(url)
                        if (!cookies.isNullOrEmpty()) {
                            append(HttpHeaders.Cookie, cookies)
                        }
                    }
                }

                                response.headers.getAll(HttpHeaders.SetCookie)?.forEach { cookieStr ->
                    cookieManager.setCookie(url, cookieStr)
                }
                cookieManager.flush()

                val statusCode = response.status.value

                                if (statusCode in 300..399) {
                    if (request.isForMainFrame) {
                        val location = response.headers[HttpHeaders.Location]
                        if (location != null) {
                            val absoluteLocation = resolveAbsoluteUrl(url, location)
                            val html = "<html><script>window.location.replace('$absoluteLocation');</script></html>"
                            return@runBlocking WebResourceResponse(
                                "text/html",
                                "UTF-8",
                                200,
                                "OK",
                                mapOf("Cache-Control" to "no-cache"),
                                html.byteInputStream()
                            )
                        }
                    }
                    return@runBlocking null
                }

                val contentTypeHeader = response.headers[HttpHeaders.ContentType]
                var mimeType = "text/html"
                var encoding = "UTF-8"

                if (!contentTypeHeader.isNullOrBlank()) {
                    try {
                        val parsedHeader = parseHeaderValue(contentTypeHeader)
                        if (parsedHeader.isNotEmpty()) {
                            mimeType = parsedHeader[0].value.substringBefore(";")
                            val charsetParam = parsedHeader[0].params.find { it.name.equals("charset", ignoreCase = true) }
                            if (charsetParam != null) {
                                encoding = charsetParam.value
                            }
                        }
                    } catch (e: ParseException) {
                        mimeType = contentTypeHeader.substringBefore(";")
                        if (contentTypeHeader.contains("charset=")) {
                            encoding = contentTypeHeader.substringAfter("charset=").substringBefore(";")
                        }
                    }
                }

                val responseHeadersMap = mutableMapOf<String, String>()
                response.headers.forEach { name, values ->
                                        if (!name.equals("Content-Encoding", ignoreCase = true)) {
                        responseHeadersMap[name] = values.joinToString(", ")
                    }
                }

                val inputStream = response.bodyAsChannel().toInputStream()

                WebResourceResponse(
                    mimeType,
                    encoding,
                    statusCode,
                    response.status.description.ifBlank { "OK" },
                    responseHeadersMap,
                    inputStream
                )
            }
        } catch (e: Exception) {
            Log.e("WebViewInterceptor", "Error intercepting request: $url", e)
            return null
        }
    }

    private fun resolveAbsoluteUrl(baseUrl: String, location: String): String {
        return try {
            URI(baseUrl).resolve(location).toString()
        } catch (e: Exception) {
            location
        }
    }
}


class WebPostBridge {
    @JavascriptInterface
    fun register(id: String, body: String, type: String) {
        WebViewRequestInterceptor.registerPostData(id, body, type)
    }
}