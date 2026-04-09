package com.easylaw.app.data.models.sample

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class SampleReqModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("pw")
    val pw: String,
    @SerializedName("adr")
    val adr: String,
)
