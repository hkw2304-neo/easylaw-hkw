package com.easylaw.app.data.models.sample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SampleReqModel(
    @SerialName("id")
    val id: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("pw")
    val pw: String = "",
    @SerialName("adr")
    val adr: String = "",
    @SerialName("etc")
    val etc: String = "",
)
