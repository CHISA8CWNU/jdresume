/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.tool

data class CryptoResult(
    val encryptedData: String,
    val iv: String
)

expect class SecureCrypto() {
    fun encrypt(data: String): CryptoResult?
    fun decrypt(encryptedData: String, ivString: String): String?
}