/*
 * Author: CHISA_CWNU
 * Organization: Business School of China West Normal University
 */

package com.chisacwnu.hicwnu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ContributionList(
    @SerialName("app_dev")
    val appDev: List<Contributor>,

    @SerialName("jiaowu_adapter")
    val jiaowuadapter: List<Contributor>
) {
    
    @Serializable     data class Contributor(
        @SerialName("name")
        val name: String,

        @SerialName("url")
        val url: String,

        @SerialName("avatar")
        val avatar: String
    )
}