package com.easylaw.app.viewModel.community

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
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

data class CommunityUpdateViewState(
    val categoryList: Map<String, CategoryModel> = emptyMap(),
    val selectedCategoryField: Map<String, String> = emptyMap(),
    val selectedCategory: String = "ALL",
    val communityUpdateTitleField: String = "",
    val communityUpdateContentField: String = "",
    val selectedImages: List<String> = emptyList(),
    val isShowDialog: Boolean = false,
    val previewImage: String? = "",
    val isUpdateLoading: Boolean = false,
    val isUpdateErrorLoading: Boolean = false,
    // 항목별 가변 필드 값
    val categoryField: List<TemplateFieldModel> = emptyList(),
    val selectedTextFields: Map<String, String> = emptyMap(),
    val isWriteSuccess: Boolean = false,
    val isErrorMsg: String = "",
    val isGoBack: Boolean = false,
)

@HiltViewModel
class CommunityUpdateViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _commnuityUpdateViewState = MutableStateFlow(CommunityUpdateViewState())
        val commnuityUpdateViewState = _commnuityUpdateViewState.asStateFlow()

        private val updateId: Long = savedStateHandle.get<Long>("updateId") ?: 0L

        private val _isUpdateSuccess = Channel<Unit>()
        val isUpdateSuccess = _isUpdateSuccess.receiveAsFlow()

        init {
            viewModelScope.launch {
                loadCategories()
                loadCommunityUpdate()
            }
        }

        suspend fun loadCategories() {
            try {
                _commnuityUpdateViewState.update {
                    it.copy(
                        isUpdateLoading = true,
                    )
                }

                val result =
                    supabase
                        .from("categories")
                        .select()
                        .decodeList<CategoryModel>()
//                val map = result.associate { it.key to it.name }
                val map = result.associateBy { it.key }

                _commnuityUpdateViewState.update {
                    it.copy(categoryList = map)
                }
            } catch (e: Exception) {
                Log.e("Category Error", e.toString())
            } finally {
                _commnuityUpdateViewState.update {
                    it.copy(
                        isUpdateLoading = false,
                    )
                }
            }
        }

        suspend fun loadCommunityUpdate() {
            try {
                val updatePost =
                    supabase
                        .from("community")
                        .select {
                            filter {
                                eq("id", updateId)
                            }
                        }.decodeSingle<CommunityWriteModel>()
//            Log.d("CategoryCheck", "DB에서 가져온 카테고리: '${updatePost.category}'")

                val initialKey =
                    _commnuityUpdateViewState.value.categoryList
                        .filterValues { it.name == updatePost.category }
                        .keys
                        .firstOrNull() ?: "ETC"

                val selectedCategory = _commnuityUpdateViewState.value.categoryList[initialKey]
                val fieldsTemplate = selectedCategory?.fields ?: emptyList()
                val savedExtraData = updatePost.extraData ?: emptyMap()

                Log.d("categoryList", _commnuityUpdateViewState.value.categoryList.toString())
                Log.d("fieldsTemplate", fieldsTemplate.toString())
                Log.d("selectedCategzory", selectedCategory.toString())
                Log.d("savedExtraData", savedExtraData.toString())

                _commnuityUpdateViewState.update {
                    it.copy(
                        communityUpdateTitleField = updatePost.title, // 제목 매핑
                        communityUpdateContentField = updatePost.content, // 내용 매핑
                        selectedCategory = initialKey, // 카테고리 매핑
                        selectedImages = updatePost.images,
                        categoryField = fieldsTemplate,
                        selectedTextFields = savedExtraData,
                    )
                }
            } catch (e: Exception) {
                Log.e("mapping error", e.toString())
            }
        }

        fun onContentSelectedFieldChanged(
            content: String,
            id: String,
        ) {
            _commnuityUpdateViewState.update { it ->
                val updatedMap = it.selectedTextFields.toMutableMap()
                updatedMap[id] = content
                it.copy(
                    selectedTextFields = updatedMap,
                )
            }
        }

        fun onCategorySelected(category: String) {
            _commnuityUpdateViewState.update {
                it.copy(selectedCategory = category)
            }
        }

        fun onTitleFieldChanged(title: String) {
            _commnuityUpdateViewState.update { it.copy(communityUpdateTitleField = title) }
        }

        fun onContentFieldChanged(content: String) {
            _commnuityUpdateViewState.update { it.copy(communityUpdateContentField = content) }
        }

        // 선택한 이미지 문자열로 저장
        fun onImageAdded(uri: String) {
            _commnuityUpdateViewState.update {
                it.copy(selectedImages = it.selectedImages + uri)
            }
        }

        fun removeSelectedImage(uri: String) {
            _commnuityUpdateViewState.update {
                it.copy(selectedImages = it.selectedImages - uri)
            }
        }

        fun onShowDialog() {
            _commnuityUpdateViewState.update {
                it.copy(isShowDialog = true)
            }
        }

        fun closeShowDialog() {
            _commnuityUpdateViewState.update {
                it.copy(isShowDialog = false)
            }
        }

        fun onImagePreview(uri: String) {
            _commnuityUpdateViewState.update { it.copy(previewImage = uri) }
        }

        fun onImagePreviewDismissed() {
            _commnuityUpdateViewState.update { it.copy(previewImage = "") }
        }

        fun updateTemplateField() {
            val selectedCategoryKey = _commnuityUpdateViewState.value.selectedCategory

            val selectedCategory = _commnuityUpdateViewState.value.categoryList[selectedCategoryKey]
            val selectedFields = selectedCategory?.fields ?: emptyList()

            val selectedTextFields =
                selectedFields.associate { field ->
                    field.id to ""
                }

            _commnuityUpdateViewState.update {
                it.copy(
                    categoryField = selectedFields,
                    selectedTextFields = selectedTextFields,
                )
            }
        }

        fun updateCommunity() {
            viewModelScope.launch {
                try {
                    _commnuityUpdateViewState.update {
                        it.copy(
                            isUpdateLoading = true,
                            isUpdateErrorLoading = false,
                        )
                    }

                    val state = _commnuityUpdateViewState.value
                    val selectedCategory = state.categoryList[state.selectedCategory]?.name ?: "기타"

                    val alreadyImg = state.selectedImages.filter { it.startsWith("http") }
                    val newUrl = state.selectedImages.filter { !it.startsWith("http") }

                    val newImg =
                        if (newUrl.isNotEmpty()) {
                            uploadImagesToStorage(newUrl) // 새 이미지만 던짐
                        } else {
                            emptyList()
                        }

                    val finalImg = alreadyImg + newImg

//                Log.d("finalImg", finalImg.size.toString())
//                Log.d("alreadyImg", alreadyImg.toString())
//                Log.d("newImg", newImg.toString())
//                Log.d("state.selectedImages", state.selectedImages.toString())

                    val updateModel =
                        CommunityWriteModel(
                            category = selectedCategory,
                            title = state.communityUpdateTitleField,
                            content = state.communityUpdateContentField,
                            author = userSession.getUserState().name,
//                            images = state.selectedImages,
                            images = if (finalImg.size == 0) emptyList() else finalImg,
                            extraData = state.selectedTextFields,
//                        images = uploadedImageUrls,
                        )
                    supabase.from("community").update(updateModel) {
                        filter {
                            eq("id", updateId)
                        }
                    }
                    // 성공 신호 보내기
                    _isUpdateSuccess.send(Unit)
                } catch (e: Exception) {
                    _commnuityUpdateViewState.update {
                        it.copy(
                            isUpdateErrorLoading = true,
                        )
                    }
                    Log.e("update error", e.toString())
                } finally {
                    _commnuityUpdateViewState.update {
                        it.copy(
                            isUpdateLoading = false,
                        )
                    }
                }
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
