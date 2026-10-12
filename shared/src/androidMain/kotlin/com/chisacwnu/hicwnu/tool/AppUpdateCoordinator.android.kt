/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import org.koin.core.annotation.Single
import java.io.File

@Single
class AndroidAppUpdateCoordinator(
    private val context: Context
) : AppUpdateCoordinator {

    private companion object {
        const val TAG = "AppUpdateCoordinator"
    }

    private fun authority(): String = "${context.packageName}.fileprovider"

    private inline fun safelyStartActivity(intent: Intent, onFallback: () -> Unit = {}) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "无法拉起安装界面：${e.message}")
            onFallback()
        }
    }

    override fun canRequestPackageInstalls(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    override fun startInstall(apkFilePath: String): Boolean {
        val apkFile = File(apkFilePath)
        if (!apkFile.exists()) {
            Log.w(TAG, "安装包不存在：$apkFilePath")
            return false
        }

        val uri: Uri = try {
            FileProvider.getUriForFile(context, authority(), apkFile)
        } catch (e: IllegalArgumentException) {

            Log.w(TAG, "FileProvider URI 构造失败，请检查 file_paths.xml", e)
            return false
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            Log.w(TAG, "已拉起安装界面: $uri")
            true
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "系统无可用安装器：${e.message}")
            false
        } catch (e: SecurityException) {

            Log.w(TAG, "拉起安装被系统拒绝（可能缺少未知来源授权）", e)
            false
        }
    }

    override fun openInstallPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safelyStartActivity(intent) { openAppSettings() }
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${context.packageName}".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        safelyStartActivity(intent)
    }

    override fun exitApp() {

        val intent = Intent().apply {
            setClassName(context, "com.chisacwnu.hicwnu.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        runCatching { context.startActivity(intent) }
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}