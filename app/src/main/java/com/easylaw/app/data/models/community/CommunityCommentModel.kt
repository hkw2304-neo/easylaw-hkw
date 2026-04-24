package com.easylaw.app.data.models.community

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityCommentModel(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("post_id")
    val post_id: Long,
    @SerialName("user_id")
    val user_id: String,
    @SerialName("author")
    val author: String,
    @SerialName("content")
    val content: String,
    @SerialName("parent_id")
    val parent_id: Long? = null,
    @SerialName("created_at")
    val created_at: String? = null,
    // 서버에 like_count라고 저장되어 있는걸 카멜케이스로 잡기 위함
    @SerialName("like_count")
    val likeCountList: List<LikeCountResponse> = emptyList(),
    @SerialName("is_liked")
    val likeUserList: List<LikeUserResponse> = emptyList(),
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val rePlyCount: Int = 0,
    val replies: List<CommunityCommentModel> = emptyList(),
)

@Serializable
data class LikeCountResponse(
    val count: Int,
)

@Serializable
data class LikeUserResponse(
    val user_id: String,
)
