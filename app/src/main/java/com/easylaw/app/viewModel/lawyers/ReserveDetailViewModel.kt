package com.easylaw.app.viewModel.lawyers

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.data.models.lawer.LaywersReserveReqModel
import com.easylaw.app.ui.components.MineType
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReserveDetailViewState(
    val reserveItem: LaywersReserveReqModel =
        LaywersReserveReqModel(
            userName = "",
            userEmail = "",
            detailTitle = "",
            detailContent = "",
            selectedCategoryName = "",
            uploadFileList = emptyList(),
        ),
    val isLoading: Boolean = false,
    val previewImage: PreviewImageType =
        PreviewImageType(
            previewImage = "",
            mineType = MineType.IMAGE,
        ),
)

data class PreviewImageDetailType(
    val previewImage: String = "",
    val mineType: MineType = MineType.IMAGE,
)

@HiltViewModel
class ReserveDetailViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        val reserveId: Long = savedStateHandle.get<Long>("reserveId") ?: 0L

        private val _reserveDetailViewState = MutableStateFlow(ReserveDetailViewState())
        val reserveDetailViewState = _reserveDetailViewState.asStateFlow()

        init {
            Log.d("예약 상세 id", reserveId.toString())
            loadReserveDetailData {
                reserveDetailItem()
            }
        }

        fun loadReserveDetailData(loadFunc: suspend () -> Unit) {
            viewModelScope.launch {
                try {
                    _reserveDetailViewState.update {
                        it.copy(
                            isLoading = true,
                        )
                    }

                    loadFunc()

                    _reserveDetailViewState.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                } catch (e: Exception) {
                    Log.e("예약 상세 불러오기 실패", e.toString())
                    _reserveDetailViewState.update {
                        it.copy(
                            isLoading = true,
                        )
                    }
                }
            }
        }

        suspend fun reserveDetailItem() {
            val res =
                supabase
                    .from("lawyers_reservations")
                    .select {
                        filter {
                            eq("id", reserveId)
                        }
                    }.decodeSingle<LaywersReserveReqModel>()

            _reserveDetailViewState.update {
                it.copy(
                    reserveItem = res,
                )
            }
//        Log.d("선택된 상담 상세", res.toString())
        }

        fun onFilePreview(image: FileUploadModel) {
            when {
                image.mimeType.contains("image") -> {
                    Log.d("mineType", image.mimeType)
                    _reserveDetailViewState.update {
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
                    _reserveDetailViewState.update {
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
                    _reserveDetailViewState.update {
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
            _reserveDetailViewState.update {
                it.copy(
                    previewImage =
                        PreviewImageType(
                            previewImage = "",
                            mineType = MineType.IMAGE,
                        ),
                )
            }
        }

        fun downloadFile(
            context: Context,
            url: String,
            fileName: String,
        ) {
            try {
                val request =
                    DownloadManager
                        .Request(Uri.parse(url))
                        .setTitle(fileName)
                        .setDescription("파일을 다운로드 중입니다...")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)

                Toast.makeText(context, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DownloadError", "다운로드 실패: ${e.message}")
                Toast.makeText(context, "다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
