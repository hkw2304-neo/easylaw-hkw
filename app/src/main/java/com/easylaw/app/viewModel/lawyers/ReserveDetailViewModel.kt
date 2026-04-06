package com.easylaw.app.viewModel.lawyers

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.lawer.LaywersReserveReqModel
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
    }
