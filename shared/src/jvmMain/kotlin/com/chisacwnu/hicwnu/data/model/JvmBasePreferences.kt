/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import com.chisacwnu.hicwnu.data.di.AppStorage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.Properties


abstract class JvmBasePreferences(
    private val fileName: String
) : KoinComponent {

    private val appStorage: AppStorage by inject()

    protected val configFile: File by lazy {
        File(appStorage.filesDir.toString(), fileName)
    }

    protected val properties = Properties()

    init {
        load()
    }

    
    protected fun load() {
        if (configFile.exists()) {
            runCatching {
                configFile.inputStream().use { properties.load(it) }
            }
        }
    }

    
    protected fun save(comments: String = "JVM Platform Preferences Storage") {
        runCatching {
            configFile.outputStream().use {
                properties.store(it, comments)
            }
        }
    }

    
    protected fun getString(key: String, defaultValue: String = ""): String {
        return properties.getProperty(key, defaultValue)
    }

    
    protected fun putString(key: String, value: String?) {
        if (value.isNullOrEmpty()) {
            properties.remove(key)
        } else {
            properties.setProperty(key, value)
        }
        save()
    }

    protected fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return properties.getProperty(key)?.toBoolean() ?: defaultValue
    }

    protected fun putBoolean(key: String, value: Boolean) {
        properties.setProperty(key, value.toString())
        save()
    }

    protected fun getInt(key: String, defaultValue: Int = 0): Int {
        return properties.getProperty(key)?.toIntOrNull() ?: defaultValue
    }

    protected fun putInt(key: String, value: Int) {
        properties.setProperty(key, value.toString())
        save()
    }

    protected fun getLong(key: String, defaultValue: Long = 0L): Long {
        return properties.getProperty(key)?.toLongOrNull() ?: defaultValue
    }

    protected fun putLong(key: String, value: Long) {
        properties.setProperty(key, value.toString())
        save()
    }

    protected fun removeKey(key: String) {
        if (properties.containsKey(key)) {
            properties.remove(key)
            save()
        }
    }

    
    fun clear() {
        properties.clear()
        save()
    }
}