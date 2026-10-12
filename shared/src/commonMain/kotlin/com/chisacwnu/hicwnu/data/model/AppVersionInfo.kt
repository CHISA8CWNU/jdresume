/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

@OptIn(ExperimentalSerializationApi::class)
object LenientIntSerializer : KSerializer<Int> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LenientInt", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeInt()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content.trim().toIntOrNull() ?: element.intOrNull ?: 0
            else -> 0
        }
    }

    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeInt(value)
}

@Serializable
data class AppVersionInfo(
    @SerialName("versionCode")
    @Serializable(with = LenientIntSerializer::class)
    val versionCode: Int = 0,
    @SerialName("versionName")
    val versionName: String = "",
    @SerialName("apkUrl")
    val apkUrl: String = "",
    @SerialName("apkSize")
    val apkSize: Long = 0L,
    @SerialName("notes")
    val notes: String = "",
    @SerialName("forceUpdate")
    val forceUpdate: Boolean = true,
    @SerialName("publishedAt")
    val publishedAt: String = "",
    @SerialName("sha256")
    val sha256: String = ""
) {

    fun hasUpdateThan(localVersionCode: Int): Boolean = versionCode > localVersionCode

    fun isValid(): Boolean = versionCode > 0 && apkUrl.isNotBlank()
}

const val VERSION_JSON_BASE_URL =
    "https://gitee.com/cwnu6/shidakechengbiao/raw/master/version.json"