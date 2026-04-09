package com.easylaw.app.viewModel.community

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.api.CommunityApiService
import com.easylaw.app.data.models.common.CategoryModel
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.data.models.common.TemplateFieldModel
import com.easylaw.app.data.models.community.CommunityWriteModel
import com.easylaw.app.data.repository.community.CommunityRepo
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.util.Common.createMultipartBody
import com.easylaw.app.util.Common.getBytesFromUri
import com.easylaw.app.util.Common.getFileUploadModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
//    val selectedImages: List<String> = emptyList(),
    val uploadFileList: List<FileUploadModel> = emptyList(),
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
        @ApplicationContext private val context: Context,
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
        private val communityRepo: CommunityRepo,
        private val service: CommunityApiService,
    ) : ViewModel() {
        private val _commnuityWriteViewState = MutableStateFlow(CommunityWriteViewState())
        val commnuityWriteViewState = _commnuityWriteViewState.asStateFlow()

        init {
//            Log.d("ViewModel_LifeCycle", "CommunityWriteViewModel 생성 (HashCode: ${this.hashCode()})")
            viewModelScope.launch {
                loadCategories()
            }
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
//        fun onImageAdded(uri: String) {
//            _commnuityWriteViewState.update {
//                it.copy(selectedImages = it.selectedImages + uri)
//            }
//        }
        // 갤러리에서 선택시 실행
        fun onFileSelected(
            context: Context,
            uri: String,
        ) {
            val fileModel = getFileUploadModel(context, uri)
            Log.d("글쓰기", fileModel.toString())
            _commnuityWriteViewState.update {
                it.copy(
                    uploadFileList = it.uploadFileList + fileModel,
                )
            }
        }

        fun removeSelectedImage(uri: String) {
            _commnuityWriteViewState.update {
                val updateFileList = it.uploadFileList.filterNot { it.uri == uri }

                it.copy(uploadFileList = updateFileList)
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

        fun writeCommunity(context: Context) {
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

                    val uploadedFiles =
                        state.uploadFileList.map { item ->
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

                    val writeModel =
                        CommunityWriteModel(
                            category = selectedCategory,
                            title = state.communityWriteTitleField,
                            content = state.communityWriteContentField,
                            author = userSession.getUserState().name,
//                            images = state.selectedImages,
                            images = uploadedFiles,
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

                    Log.d("글쓰기 완", "완")
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

        // 프로젝트가 방대해진다고 굳이 Repo 폴더로 따로 팔 필요가 있나??
        // api 통신하면서 여기서 받고 가공도 해서 화면에 뿌려주기 위한 작업을 하는데?
        fun postCommunityWithFile() {
            viewModelScope.launch {
                try {
                    val data =
                        mapOf(
                            "id" to "user123",
                            "name" to "kiwon",
                            "pw" to "1234",
                            "adr" to "seoul",
                            "etc" to "test",
                        )
//                Log.d("req", req.toString())

//                communityRepo.postCommunity(req = req, context = context)

                    val tempFiles = mutableListOf<MultipartBody.Part>()
                    val filesList = _commnuityWriteViewState.value.uploadFileList

                    // Json으로 묶어서 보내기
                    // map 객체에서 특정 필드만 제외
//                val textData = req.filter { it.key != "files" }
                    // Json 변형
                    val dataToJosn = Gson().toJson(data)

                    // 타입 application/json 명시
                    val dataPart = dataToJosn.toRequestBody("application/json".toMediaTypeOrNull())

                    val filePart =
                        when {
                            filesList.isEmpty() -> null
                            else -> {
                                filesList.forEach { item ->
                                    val uri = Uri.parse(item.uri)
                                    createMultipartBody(context, uri)?.let {
                                        tempFiles.add(it)
                                    }
                                }
                                tempFiles
                            }
                        }

                    val req = service.uploadCommunity(data = dataPart, file = filePart)
                } catch (e: Exception) {
                    Log.e("첨부파일 업로드 에러", e.toString())
                }
            }
        }
    }
