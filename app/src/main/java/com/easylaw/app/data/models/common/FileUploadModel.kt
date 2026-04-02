package com.easylaw.app.data.models.common

import kotlinx.serialization.Serializable

@Serializable
data class FileUploadModel(
    val uri: String = "",
    val name: String = "",
    val mimeType: String = "",
    val dataSize: String = "",
)
