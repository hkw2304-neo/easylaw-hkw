package com.easylaw.app.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.CommentModel
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.util.Common
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailViewState(
    val communityDetail: CommunityWriteModel? = null,
    val isLoading: Boolean = false,
    val previewImage: String? = "",
    val showCommentSheet: Boolean = false,
    val commentInput: String = "",
)

@HiltViewModel
class CommunityDetailViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
    ) : ViewModel() {
    /*
        savedStateHandle: SavedStateHandle,
        자동으로 arguments를 가로챈다
     */
        private val id: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

        private val _communityDetailViewState = MutableStateFlow(CommunityDetailViewState())
        val communityDetailViewState = _communityDetailViewState.asStateFlow()

        init {
            viewModelScope.launch {
                loadCommunityDetail(id = id)
            }
        }

        fun onValueChanged(comment: String) {
            _communityDetailViewState.update {
                it.copy(commentInput = comment)
            }
        }

        fun onShowCommentSheet() {
            _communityDetailViewState.update {
                it.copy(showCommentSheet = true)
            }
        }

        fun closeShowCommentSheet() {
            _communityDetailViewState.update {
                it.copy(showCommentSheet = false)
            }
        }

        fun onImagePreview(uri: String) {
            _communityDetailViewState.update { it.copy(previewImage = uri) }
        }

        fun onImagePreviewDismissed() {
            _communityDetailViewState.update { it.copy(previewImage = "") }
        }

        suspend fun loadCommunityDetail(id: Long) {
            try {
//            Log.d("id", id.toString())
                _communityDetailViewState.update {
                    it.copy(
                        isLoading = true,
                    )
                }

                val result =
                    supabase
                        .from("community")
                        .select {
                            filter {
                                eq("id", id)
                            }
                        }.decodeSingle<CommunityWriteModel>()

                _communityDetailViewState.update {
                    it.copy(
                        communityDetail = result,
                        isLoading = false,
                    )
                }
                Log.d("상세보기", "데이터 조회 성공: $result")
            } catch (e: Exception) {
                Log.e("Supabase", "데이터 조회 실패: ${e.message}")
            } finally {
                _communityDetailViewState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
            }
        }

        fun sendComment(inputComment: String) {
            if (inputComment.isEmpty()) return
            viewModelScope.launch {
                val author = userSession.getUserState().name
                val createdAt =
                    Common.formatIsoDate(
                        java.time.Instant
                            .now()
                            .toString(),
                    )

                val newComment =
                    CommentModel(
                        author = author,
                        content = inputComment,
                        created_at = createdAt,
                    )

                addComment(newComment)
                _communityDetailViewState.update {
                    it.copy(
                        commentInput = "",
                    )
                }
            }
        }

        suspend fun addComment(newComment: CommentModel) {
            try {
//                _communityDetailViewState.update {
//                    it.copy(
//                        isLoading = true
//                    )
//                }

                val currentComments = _communityDetailViewState.value.communityDetail?.comments ?: emptyList()

                val updatedComments = currentComments + newComment
                supabase
                    .from("community")
                    .update(mapOf("comments" to updatedComments)) {
                        filter { eq("id", id) }
                    }
                _communityDetailViewState.update {
                    it.copy(
                        communityDetail = it.communityDetail?.copy(comments = updatedComments),
                    )
                }
            } catch (e: Exception) {
                Log.e("Comment Error", e.toString())
            }
//            finally {
//                _communityDetailViewState.update {
//                    it.copy(
//                        isLoading = false
//                    )
//                }
//            }
        }
    }
