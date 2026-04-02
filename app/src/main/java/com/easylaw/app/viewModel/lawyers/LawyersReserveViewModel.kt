package com.easylaw.app.viewModel.lawyers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.common.CategoryModel
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.data.models.lawer.LaywersReserveReqModel
import com.easylaw.app.domain.model.UserInfo
import com.easylaw.app.ui.components.MineType
import com.easylaw.app.util.Common.getFileUploadModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

enum class LawyersReserveInputType {
    Title,
    Content,
}

data class LawyersReserveInputFormState(
    val userName: String = "",
    val userEmail: String = "",
    val detailTitle: String = "",
    val detailContent: String = "",
    val selectedCategoryName: String = "",
    val uploadFileList: List<FileUploadModel> = emptyList(),
)

data class PreviewImageType(
    val previewImage: String = "",
    val mineType: MineType = MineType.IMAGE,
)

data class LawyersReserveViewState(
    val userId: String = "",
    val userInfo: UserInfo =
        UserInfo(
            id = "",
            name = "",
            email = "",
            user_role = "",
        ),
    val categoryList: List<CategoryModel> = emptyList(),
    val categoryExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val lawyersReserveInputForm: LawyersReserveInputFormState = LawyersReserveInputFormState(),
    val isShowSelectFiles: Boolean = false,
    val previewImage: PreviewImageType =
        PreviewImageType(
            previewImage = "",
            mineType = MineType.IMAGE,
        ),
    val isWriteSuccess: Boolean = false,
    val isErrorMsg: String = "",
    val isGoBack: Boolean = false,
)

@HiltViewModel
class LawyersReserveViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val supabase: SupabaseClient,
    ) : ViewModel() {
        private val _lawyersReserveViewState = MutableStateFlow(LawyersReserveViewState())
        val lawyersReserveViewState = _lawyersReserveViewState.asStateFlow()

        private val _userId: String = savedStateHandle.get<String>("userId") ?: ""

        init {

            _lawyersReserveViewState.update {
                it.copy(
                    userId = _userId,
                )
            }
            viewModelScope.launch {
                loadLaywerReserveData()
            }
        }

        suspend fun loadLaywerReserveData() {
            try {
                _lawyersReserveViewState.update {
                    it.copy(
                        isLoading = true,
                    )
                }

                // 하나의 함수에서 n개 요청 처리 시
                // 1개여도 사용되지만 굳이 그럴 필요가 없다.
                coroutineScope {
                    val res =
                        async {
                            supabase
                                .from("users")
                                .select {
                                    filter {
                                        eq("id", _userId)
                                    }
                                }.decodeSingle<UserInfo>()
                        }

                    val categoryInfo =
                        async {
                            supabase.from("categories").select().decodeList<CategoryModel>()
                        }

                    val userInfo = res.await()
                    val categoryList = categoryInfo.await()
//                Log.d("init", "init 2")

                    _lawyersReserveViewState.update {
                        it.copy(
                            userInfo = userInfo,
                            categoryList = categoryList,
                            isLoading = false,
                            lawyersReserveInputForm =
                                LawyersReserveInputFormState(
                                    userName = userInfo.name,
                                    userEmail = userInfo.email,
                                ),
                        )
                    }
//                Log.d("init", "init 3")
//                Log.d("categoryList", _lawyersReserveViewState.value.categoryList.toString())
                }
                Log.d("입력 폼 정보", _lawyersReserveViewState.value.lawyersReserveInputForm.toString())
            } catch (e: Exception) {
                _lawyersReserveViewState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
                Log.e("상담요청 유저 데이터 에러", e.toString())
            }
        }

        fun onExpandedChange() {
            _lawyersReserveViewState.update {
                it.copy(
                    categoryExpanded = !it.categoryExpanded,
                )
            }
        }

        fun onValueChange(categoryName: String) {
            _lawyersReserveViewState.update {
                it.copy(
//                selectedCategoryName = categoryName
                    lawyersReserveInputForm =
                        it.lawyersReserveInputForm.copy(
                            selectedCategoryName = categoryName,
                        ),
                )
            }
        }

        fun onClick(categoryName: String) {
            _lawyersReserveViewState.update {
                it.copy(
//                selectedCategoryName = categoryName,
                    lawyersReserveInputForm =
                        it.lawyersReserveInputForm.copy(
                            selectedCategoryName = categoryName,
                        ),
                    categoryExpanded = !it.categoryExpanded,
                )
            }
        }

        fun onValueChangeInput(
            value: String,
            type: LawyersReserveInputType,
        ) {
//        Log.d("인풋 변경", "value : ${value} , type : ${type}")
            when (type) {
                LawyersReserveInputType.Title ->
                    _lawyersReserveViewState.update {
                        it.copy(
                            lawyersReserveInputForm =
                                it.lawyersReserveInputForm.copy(
                                    detailTitle = value,
                                ),
                        )
                    }
                LawyersReserveInputType.Content ->
                    _lawyersReserveViewState.update {
                        it.copy(
                            lawyersReserveInputForm =
                                it.lawyersReserveInputForm.copy(
                                    detailContent = value,
                                ),
                        )
                    }
            }
        }

        fun showSelectFiles() {
            _lawyersReserveViewState.update {
                it.copy(
                    isShowSelectFiles = true,
                )
            }
        }

        fun closeSelectFiles() {
            _lawyersReserveViewState.update {
                it.copy(
                    isShowSelectFiles = false,
                )
            }
        }

        fun onFileSelected(
            context: Context,
            uri: String,
        ) {
            val fileModel = getFileUploadModel(context, uri)

            _lawyersReserveViewState.update {
                it.copy(
                    lawyersReserveInputForm =
                        it.lawyersReserveInputForm.copy(
                            uploadFileList = it.lawyersReserveInputForm.uploadFileList + fileModel,
                        ),
                )
            }

//        _lawyersReserveViewState.update {
//            it.copy(
//                lawyersReserveInputForm = LawyersReserveInputFormState(
//                    uploadFileList = it.lawyersReserveInputForm.uploadFileList + fileModel
//                )
//            )
//        }
        }

        fun onFilePreview(image: FileUploadModel) {
            when {
                image.mimeType.contains("image") -> {
                    Log.d("mineType", image.mimeType)
                    _lawyersReserveViewState.update {
                        it.copy(
                            previewImage =
                                PreviewImageType(
                                    previewImage = image.uri,
                                    mineType = MineType.IMAGE,
                                ),
                        )
                    }
                }
                image.mimeType.contains("video") -> {
                    Log.d("mineType", image.mimeType)
                    _lawyersReserveViewState.update {
                        it.copy(
                            previewImage =
                                PreviewImageType(
                                    previewImage = image.uri,
                                    mineType = MineType.VIDEO,
                                ),
                        )
                    }
                }
                image.mimeType.contains("pdf") -> {
                    Log.d("mineType", image.mimeType)
                    _lawyersReserveViewState.update {
                        it.copy(
                            previewImage =
                                PreviewImageType(
                                    previewImage = image.uri,
                                    mineType = MineType.PDF,
                                ),
                        )
                    }
                }
                else -> {
                    Log.d("지원하지않는 양식", image.mimeType)
                }
            }
        }

        fun onImagePreviewDismissed() {
            _lawyersReserveViewState.update {
                it.copy(
                    previewImage =
                        PreviewImageType(
                            previewImage = "",
                            mineType = MineType.IMAGE,
                        ),
                )
            }
        }

        fun removeSelectedFile(uri: String) {
            _lawyersReserveViewState.update { state ->
                val updateFileList = state.lawyersReserveInputForm.uploadFileList.filterNot { it.uri == uri }

                state.copy(
                    lawyersReserveInputForm =
                        state.lawyersReserveInputForm.copy(
                            uploadFileList = updateFileList,
                        ),
                )
            }
        }

        fun onClickReserve(context: Context) {
            viewModelScope.launch {
                try {
                    _lawyersReserveViewState.update {
                        it.copy(
                            isLoading = true,
                        )
                    }

                    val currentForm = _lawyersReserveViewState.value.lawyersReserveInputForm

                    // 첨부파일은 버킷에 먼저 업로드
                    val uploadedFiles =
                        currentForm.uploadFileList.map { fileModel ->
                            val fileName = "${System.currentTimeMillis()}_${fileModel.name}"
                            val filePath = "reservations/$fileName"

                            // 파일을 바이트 배열로 변환 (추후 구현할 유틸 함수)
                            val fileBytes = getBytesFromUri(context, Uri.parse(fileModel.uri))

                            // Storage 업로드
                            val bucket = supabase.storage.from("lawyer_files")
                            bucket.upload(filePath, fileBytes)

                            // 업로드된 파일의 Public URL 생성
                            val publicUrl = bucket.publicUrl(filePath)

                            // DB 저장을 위해 URI를 변환된 URL로 교체한 새 모델 생성
                            fileModel.copy(uri = publicUrl)
                        }

                    val request =
                        LaywersReserveReqModel(
                            userName = currentForm.userName,
                            userEmail = currentForm.userEmail,
                            detailTitle = currentForm.detailTitle,
                            detailContent = currentForm.detailContent,
                            selectedCategoryName = currentForm.selectedCategoryName,
                            uploadFileList = uploadedFiles,
                        )

                    supabase.from("lawyers_reservations").insert(request)

                    Log.d("Supabase", "예약 등록 성공!")
                    _lawyersReserveViewState.update {
                        it.copy(
                            isLoading = false,
                            isWriteSuccess = true,
                        )
                    }
                } catch (e: Exception) {
                    Log.e("Supabase", "예약 실패: ${e.message}")
                    _lawyersReserveViewState.update {
                        it.copy(
                            isLoading = true,
                            isErrorMsg = e.toString(),
                        )
                    }
                }
            }
        }

        fun closeShowDialog() {
            _lawyersReserveViewState.update {
                it.copy(
                    isWriteSuccess = false,
                    isErrorMsg = "",
                    isGoBack = true,
                )
            }
        }

        private fun getBytesFromUri(
            context: Context,
            uri: Uri,
        ): ByteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: byteArrayOf()
    }
