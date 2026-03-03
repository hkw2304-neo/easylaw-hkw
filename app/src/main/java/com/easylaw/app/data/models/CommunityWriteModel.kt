package com.easylaw.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CommunityWriteModel(
    // 기본키
    val id: Long? = null,
    // 로그인 유저의 id
    val user_id: String? = null,
    val created_at: String = "",
    val category: String,
    val title: String,
    val content: String,
    val author: String,
    val images: List<String> = emptyList(),
)
