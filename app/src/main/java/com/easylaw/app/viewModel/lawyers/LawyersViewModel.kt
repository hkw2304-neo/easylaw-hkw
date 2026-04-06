package com.easylaw.app.viewModel.lawyers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.lawer.LawyersModel
import com.easylaw.app.data.models.lawer.LaywersReserveReqModel
import com.easylaw.app.domain.model.UserInfo
import com.easylaw.app.domain.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LawyersViewState(
    val userState: UserInfo =
        UserInfo(
            id = "",
            name = "",
            email = "",
            user_role = "",
        ),
    val isLoading: Boolean = false,
    val laywersList: List<LawyersModel> = emptyList(),
    val reserveList: List<LaywersReserveReqModel> = emptyList(),
    // 그리드
    val showLaywersDialog: Boolean = false,
    // 중복 로직 할 때는 list 말고 set으로 하자
    val selectedIdSet: Set<LawyersModel> = emptySet(),
    val selectedTotalChecked: Boolean = false,
)

@HiltViewModel
class LawyersViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
    ) : ViewModel() {
        private val _lawyersViewState = MutableStateFlow(LawyersViewState())
        val lawyersViewState = _lawyersViewState.asStateFlow()

        init {

            val userState = userSession.getUserState()

//        Log.d("LawyersViewModel init", "변호사 뷰모델 생성")
            _lawyersViewState.update {
                it.copy(
                    userState = userState,
                )
            }

            viewModelScope.launch {
                loadLawyers()
            }
//        Log.d("변호사 메뉴 userEmail", _lawyersViewState.value.userState.toString())
        }

        suspend fun loadLawyers() {
            try {
                _lawyersViewState.update {
                    it.copy(
                        isLoading = true,
                    )
                }

                coroutineScope {
                    val resLaywers =
                        async {
                            supabase.from("lawyers").select().decodeList<LawyersModel>()
                        }
                    val resReserve =
                        async {
                            supabase
                                .from("lawyers_reservations")
                                .select {
                                    filter {
                                        eq("user_email", _lawyersViewState.value.userState.email)
                                    }
                                }.decodeList<LaywersReserveReqModel>()
                        }

                    val laywersInfo = resLaywers.await()
                    val reserveInfo = resReserve.await()
//                Log.d("reserveInfo", "변호사 로드 성공: $reserveInfo")
                    _lawyersViewState.update {
                        it.copy(
                            laywersList = laywersInfo,
                            reserveList = reserveInfo,
                        )
                    }

                    _lawyersViewState.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _lawyersViewState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
                Log.e("LawyersViewModel", "변호사 로드 실패: ${e.message}")
            }
        }

        fun toggleLaywersDialog() {
            _lawyersViewState.update {
                it.copy(
                    showLaywersDialog = !it.showLaywersDialog,
                    selectedTotalChecked = false,
                    selectedIdSet = emptySet(),
                )
            }
        }

        // 중복 로직
        fun onGridCellClick(selectedItem: LawyersModel) {
            _lawyersViewState.update {
                val isContained = it.selectedIdSet.contains(selectedItem)
                val newItem =
                    when (isContained) {
                        true -> it.selectedIdSet - selectedItem
                        else -> it.selectedIdSet + selectedItem
                    }
                it.copy(
                    selectedIdSet = newItem,
                )
            }

            Log.d("선택된 그리드(개별)", _lawyersViewState.value.selectedIdSet.toString())
        }

        // 전체선택
        fun toggleSelectedTotalChecked() {
            _lawyersViewState.update {
                val isTotalChecked = !it.selectedTotalChecked

                val newIdSet =
                    when (isTotalChecked) {
                        true -> it.laywersList.map { it }.toSet()
                        else -> emptySet()
                    }
                it.copy(
                    selectedTotalChecked = isTotalChecked,
                    selectedIdSet = newIdSet,
                )
            }
            Log.d("선택된 그리드(전체)", _lawyersViewState.value.selectedIdSet.toString())
        }

        fun onCofirm() {
//            Log.d("선택받은 변호사", _lawyersViewState.value.selectedIdSet.toString())
            val setList = _lawyersViewState.value.selectedIdSet
//            setList.forEach{
//                item ->
//                Log.d("선택받은 변호사", item.name)
//            }
        }

        override fun onCleared() {
            Log.d("LawyersViewModel Close", "변호사 뷰모델 파괴")
            super.onCleared()
        }
    }
