package com.easylaw.app.data.models.lawer

import com.easylaw.app.data.models.common.FileUploadModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LaywersReserveReqModel(
    @SerialName("id") val id: Long = 0L,
    @SerialName("user_name") val userName: String,
    @SerialName("user_email") val userEmail: String,
    @SerialName("detail_title") val detailTitle: String,
    @SerialName("detail_content") val detailContent: String,
    @SerialName("selected_category_name") val selectedCategoryName: String,
    @SerialName("upload_file_list") val uploadFileList: List<FileUploadModel>,
    @SerialName("created_at") val createdAt: String = "",
)
