package com.easylaw.app.viewModel.community

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.common.CategoryModel
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.data.models.common.TemplateFieldModel
import com.easylaw.app.data.models.community.CommunityWriteModel
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.util.Common.getBytesFromUri
import com.easylaw.app.util.Common.getFileUploadModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUpdateViewState(
    val categoryList: Map<String, CategoryModel> = emptyMap(),
    val selectedCategoryField: Map<String, String> = emptyMap(),
    val selectedCategory: String = "ALL",
    val communityUpdateTitleField: String = "",
    val communityUpdateContentField: String = "",
//    val selectedImages: List<FileUploadModel> = emptyList(),
    val uploadFileList: List<FileUploadModel> = emptyList(),
    val isShowDialog: Boolean = false,
    val previewImage: String? = "",
    val isUpdateLoading: Boolean = false,
    val isUpdateErrorLoading: Boolean = false,
    // 항목별 가변 필드 값
    val categoryField: List<TemplateFieldModel> = emptyList(),
    val selectedTextFields: Map<String, String> = emptyMap(),
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

        init {

            updateViewDataLoad {
                coroutineScope {
                    val categoryInfo =
                        async {
                            loadCategories()
                        }
                    val updateInfo =
                        async {
                            loadCommunityUpdate()
                        }
                    categoryInfo.await()
                    updateInfo.await()
                }
            }
        }

        fun updateViewDataLoad(func: suspend () -> Unit) {
            viewModelScope.launch {
                try {
                    _commnuityUpdateViewState.update {
                        it.copy(
                            isUpdateLoading = true,
                        )
                    }

                    func()

                    _commnuityUpdateViewState.update {
                        it.copy(
                            isUpdateLoading = false,
                        )
                    }
                } catch (e: Exception) {
                    Log.e("updateViewDataLoad error", e.toString())
                    _commnuityUpdateViewState.update {
                        it.copy(
                            isUpdateLoading = false,
                        )
                    }
                }
            }
        }

        suspend fun loadCategories() {
            try {
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
                        uploadFileList = updatePost.images,
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
//        fun onImageAdded(uri: String) {
//            _commnuityUpdateViewState.update {
//                it.copy(selectedImages = it.selectedImages + uri)
//            }
//        }

        fun onFileSelected(
            context: Context,
            uri: String,
        ) {
            val fileModel = getFileUploadModel(context, uri)
            Log.d("글쓰기 수정", fileModel.toString())
            _commnuityUpdateViewState.update {
                it.copy(
                    uploadFileList = it.uploadFileList + fileModel,
                )
            }
        }

        fun removeSelectedImage(uri: String) {
            _commnuityUpdateViewState.update {
                val updateFileList = it.uploadFileList.filterNot { it.uri == uri }

                it.copy(uploadFileList = updateFileList)
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
                            isGoBack = false,
                            isUpdateLoading = true,
                            isUpdateErrorLoading = false,
                        )
                    }

                    val state = _commnuityUpdateViewState.value
                    val selectedCategory = state.categoryList[state.selectedCategory]?.name ?: "기타"

                    val alreadyImg = state.uploadFileList.filter { it.uri.startsWith("http") }
                    val newUrl = state.uploadFileList.filter { !it.uri.startsWith("http") }

                    // .map 원본 리스트는 건드리지않고 요소로 작업을 하기 위함
                    val newImg =
                        newUrl.map { item ->
                            val fileName = "${System.currentTimeMillis()}_${item.name}"
                            val filePath = "community/$fileName"
                            val fileBytes = getBytesFromUri(context, Uri.parse(item.uri))
                            val bucket = supabase.storage.from("community")
                            bucket.upload(
                                path = filePath,
                                data = fileBytes,
                            )
                            val publicUrl = bucket.publicUrl(filePath)
                            item.copy(uri = publicUrl)
                        }

                    val finalImg = alreadyImg + newImg
                    Log.d("수정 사진 사이즈", finalImg.size.toString())

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
                            images = finalImg,
                            extraData = state.selectedTextFields,
//                        images = uploadedImageUrls,
                        )
                    supabase.from("community").update(updateModel) {
                        filter {
                            eq("id", updateId)
                        }
                    }
                    // 성공 신호 보내기
//                _isUpdateSuccess.send(Unit)
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
                            isGoBack = true,
                            isUpdateLoading = false,
                        )
                    }
                }
            }
        }
    }
