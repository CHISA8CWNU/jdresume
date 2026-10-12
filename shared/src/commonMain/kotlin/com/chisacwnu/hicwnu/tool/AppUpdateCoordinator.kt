/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

interface AppUpdateCoordinator {

    fun canRequestPackageInstalls(): Boolean

    fun startInstall(apkFilePath: String): Boolean

    fun openInstallPermissionSettings()

    fun exitApp()
}