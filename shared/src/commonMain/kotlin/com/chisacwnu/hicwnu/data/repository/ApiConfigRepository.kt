/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chisacwnu.hicwnu.data.api.webdav.WebDavClient
import com.chisacwnu.hicwnu.data.api.webdav.WebDavConfig
import com.chisacwnu.hicwnu.tool.SecureCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single


@Single
class ApiConfigRepository(
    @Named("ApiConfig") private val dataStore: DataStore<Preferences>,
    private val secureCrypto: SecureCrypto
) {

    
    object ApiKeys {

        
        object WebDav {
            private const val PREFIX = "webdav_"
            val BASE_URL = stringPreferencesKey("${PREFIX}base_url")
            val USERNAME = stringPreferencesKey("${PREFIX}username")
            val ROOT_PATH = stringPreferencesKey("${PREFIX}root_path")
            val ENCRYPTED_PASSWORD = stringPreferencesKey("${PREFIX}encrypted_pwd")
            val CRYPTO_IV = stringPreferencesKey("${PREFIX}crypto_iv")
        }
    }

    
    val webDavConfigFlow: Flow<WebDavConfig?> = dataStore.data.map { preferences ->
        val baseUrl = preferences[ApiKeys.WebDav.BASE_URL]
        val username = preferences[ApiKeys.WebDav.USERNAME]
        val rootPath = preferences[ApiKeys.WebDav.ROOT_PATH] ?: "HiCWNU"
        val encryptedPassword = preferences[ApiKeys.WebDav.ENCRYPTED_PASSWORD]
        val ivString = preferences[ApiKeys.WebDav.CRYPTO_IV]

        if (!baseUrl.isNullOrBlank() && !username.isNullOrBlank() &&
            !encryptedPassword.isNullOrBlank() && !ivString.isNullOrBlank()
        ) {
            val decryptedPassword = secureCrypto.decrypt(encryptedPassword, ivString)

            if (decryptedPassword != null) {
                WebDavConfig(
                    baseUrl = baseUrl,
                    username = username,
                    password = decryptedPassword,
                    rootPath = rootPath
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    
    suspend fun saveWebDavConfig(config: WebDavConfig) {
        val cryptoResult = secureCrypto.encrypt(config.password) ?: return

        dataStore.edit { preferences ->
            preferences[ApiKeys.WebDav.BASE_URL] = config.baseUrl.trim()
            preferences[ApiKeys.WebDav.USERNAME] = config.username.trim()
            preferences[ApiKeys.WebDav.ROOT_PATH] = config.rootPath.trim()
            preferences[ApiKeys.WebDav.ENCRYPTED_PASSWORD] = cryptoResult.encryptedData
            preferences[ApiKeys.WebDav.CRYPTO_IV] = cryptoResult.iv
        }
    }

    
    suspend fun clearWebDavConfig() {
        dataStore.edit { preferences ->
            preferences.remove(ApiKeys.WebDav.BASE_URL)
            preferences.remove(ApiKeys.WebDav.USERNAME)
            preferences.remove(ApiKeys.WebDav.ROOT_PATH)
            preferences.remove(ApiKeys.WebDav.ENCRYPTED_PASSWORD)
            preferences.remove(ApiKeys.WebDav.CRYPTO_IV)
        }
    }

    
    suspend fun createWebDavClient(explicitConfig: WebDavConfig? = null): WebDavClient? {
        val finalConfig = explicitConfig ?: webDavConfigFlow.firstOrNull()
        return finalConfig?.let {
            WebDavClient(config = it)
        }
    }
}