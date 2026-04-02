package com.easylaw.app.viewModel.community

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.common.CategoryModel
import com.easylaw.app.data.models.common.TemplateFieldModel
import com.easylaw.app.data.models.community.CommunityWriteModel
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.util.Numbers
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CommunityWriteViewState(
//    val categories: List<String> = listOf("민사", "형사", "노무", "가사", "기타"),
//    val selectedCategory: String = "",
    val categoryList: Map<String, CategoryModel> = emptyMap(),
    val selectedCategoryField: Map<String, String> = emptyMap(),
//    val categoryList: Map<String, String> = emptyMap(),
    val selectedCategory: String = "ALL",
    // 공통 입력
    val communityWriteTitleField: String = "",
    val communityWriteContentField: String = "",
    val selectedImages: List<String> = emptyList(),
    // 항목별 가변 필드 값
    val categoryField: List<TemplateFieldModel> = emptyList(),
    val selectedTextFields: Map<String, String> = emptyMap(),
    val isShowDialog: Boolean = false,
    val previewImage: String? = "",
    val isWriteLoading: Boolean = false,
    val isWriteSuccess: Boolean = false,
    val isErrorMsg: String = "",
    val isGoBack: Boolean = false,
)

@HiltViewModel
class CommunityWriteViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _commnuityWriteViewState = MutableStateFlow(CommunityWriteViewState())
        val commnuityWriteViewState = _commnuityWriteViewState.asStateFlow()

        // 글쓰기 성공 감지(뒤로가기 용)
        // channel : 하나의 상태를 알려주기 위함
        private val _isWriteSuccess = Channel<Unit>()
        val isWriteSuccess = _isWriteSuccess.receiveAsFlow()

        init {
//            Log.d("ViewModel_LifeCycle", "CommunityWriteViewModel 생성 (HashCode: ${this.hashCode()})")
            viewModelScope.launch {
                loadCategories()
            }
        }

        override fun onCleared() {
            super.onCleared()
//        Log.d("ViewModel_LifeCycle", "CommunityWriteViewModel 파괴 (onCleared)")
        }

        suspend fun loadCategories() {
            try {
                _commnuityWriteViewState.update {
                    it.copy(
                        isWriteLoading = true,
                    )
                }

                val result =
                    supabase
                        .from("categories")
                        .select()
                        .decodeList<CategoryModel>()
//                val map = result.associate { it.key to it.name }
                // associateBy 하나의 항목을 키로 잡을 뿐 저장된다.
                val map = result.associateBy { it.key }

                _commnuityWriteViewState.update {
                    it.copy(categoryList = map)
                }
            } catch (e: Exception) {
                Log.e("Category Error", e.toString())
            } finally {
                _commnuityWriteViewState.update {
                    it.copy(
                        isWriteLoading = false,
                    )
                }
            }
        }

        fun onCategorySelected(category: String) {
            _commnuityWriteViewState.update {
                it.copy(selectedCategory = category)
            }
        }

        fun onTitleFieldChanged(title: String) {
            _commnuityWriteViewState.update { it.copy(communityWriteTitleField = title) }
        }

        fun onContentFieldChanged(content: String) {
            _commnuityWriteViewState.update { it.copy(communityWriteContentField = content) }
        }

        // 선택한 이미지 문자열로 저장
        fun onImageAdded(uri: String) {
            _commnuityWriteViewState.update {
                it.copy(selectedImages = it.selectedImages + uri)
            }
        }

        fun removeSelectedImage(uri: String) {
            _commnuityWriteViewState.update {
                it.copy(selectedImages = it.selectedImages - uri)
            }
        }

        fun onShowDialog() {
            _commnuityWriteViewState.update {
                it.copy(isShowDialog = true)
            }
        }

        fun closeShowDialog() {
            _commnuityWriteViewState.update {
                it.copy(isShowDialog = false)
            }
        }

        fun closeWriteShowDialog() {
            _commnuityWriteViewState.update {
                it.copy(
                    isWriteSuccess = false,
                    isErrorMsg = "",
                    isGoBack = true,
                )
            }
        }

        fun onImagePreview(uri: String) {
            _commnuityWriteViewState.update { it.copy(previewImage = uri) }
        }

        fun onImagePreviewDismissed() {
            _commnuityWriteViewState.update { it.copy(previewImage = "") }
        }

        fun writeCommunity() {
            viewModelScope.launch {
                try {
                    _commnuityWriteViewState.update {
                        it.copy(
                            isWriteLoading = true,
                            isErrorMsg = "",
                        )
                    }

                    // 현재 뷰에서의 상태(변수)
                    val state = _commnuityWriteViewState.value
                    val selectedCategory = state.categoryList[state.selectedCategory]?.name ?: "기타"

                    val uploadedImageUrls =
                        if (state.selectedImages.isNotEmpty()) {
                            uploadImagesToStorage(state.selectedImages)
                        } else {
                            emptyList()
                        }

                    val writeModel =
                        CommunityWriteModel(
                            category = selectedCategory,
                            title = state.communityWriteTitleField,
                            content = state.communityWriteContentField,
                            author = userSession.getUserState().name,
//                            images = state.selectedImages,
                            images = uploadedImageUrls,
                            extraData = state.selectedTextFields,
                        )
                    supabase.from("community").insert(writeModel)
                    // 성공 신호 보내기
//                    _isWriteSuccess.send(Unit)

                    _commnuityWriteViewState.update {
                        it.copy(
                            isWriteSuccess = true,
                            isWriteLoading = false,
                        )
                    }
                } catch (e: Exception) {
                    _commnuityWriteViewState.update {
                        it.copy(
                            isErrorMsg = e.toString(),
                            isWriteLoading = false,
                        )
                    }
                    Log.e("write error", e.toString())
                }
            }
        }

        fun updateTemplateField() {
            val selectedCategoryKey = _commnuityWriteViewState.value.selectedCategory

            val selectedCategory = _commnuityWriteViewState.value.categoryList[selectedCategoryKey]
            val selectedFields = selectedCategory?.fields ?: emptyList()

            val selectedTextFields =
                selectedFields.associate { field ->
                    field.id to ""
                }
            Log.d("선택한 템플릿 텍스트 필드", selectedTextFields.toString())

            _commnuityWriteViewState.update {
                it.copy(
                    categoryField = selectedFields,
                    selectedTextFields = selectedTextFields,
                )
            }
//        Log.d("선택한 템플릿 : ", selectedFields.toString())
        }

        fun onContentSelectedFieldChanged(
            content: String,
            id: String,
        ) {
//        Log.d("변하는 중", "content : ${content} , id: ${id}")
            // state 변수는 기본적으로 읽기 전용
            // 수정 가능한 형태를 가져와서 수정
            _commnuityWriteViewState.update { it ->
                val updatedMap = it.selectedTextFields.toMutableMap()
                updatedMap[id] = content
                it.copy(
                    selectedTextFields = updatedMap,
                )
            }
        }

        private suspend fun uploadImagesToStorage(uris: List<String>): List<String> {
            val publicUrls = mutableListOf<String>()

            uris.forEach { uriString ->
                val uri = Uri.parse(uriString)
                val fileName = "community_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(Numbers.FIVE)}.jpg"

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }

                if (bytes != null) {
                    val bucket = supabase.storage.from("community")

                    bucket.upload(
                        path = fileName,
                        data = bytes,
                        upsert = false,
                    )
                    val url = bucket.publicUrl(fileName)
                    publicUrls.add(url)
                }
            }
            return publicUrls
        }
    }
