package com.easylaw.app.data.models.community

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityPrecSearchModel(
    @SerializedName("PrecSearch")
    val precSearch: CommunityLawModel,
)
