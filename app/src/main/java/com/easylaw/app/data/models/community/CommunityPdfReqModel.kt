package com.easylaw.app.data.models.community

import kotlinx.serialization.Serializable

@Serializable
data class CommunityPdfReqModel(
    val postId: String,
    val title: String,
    val content: String,
    val category: String,
    val author: String,
    val extra_info: List<PdfItem>,
)

@Serializable
data class PdfItem(
    val label: String,
    val value: String? = null,
)
