package com.easylaw.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.CategoryModel
import com.easylaw.app.data.models.CommunityWriteModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityViewState(
    val isCommunityListLoading: Boolean = false,
    val communityList: List<CommunityWriteModel> = emptyList(),
    val categoryList: Map<String, String> = emptyMap(),
    val selectedCategory: String = "ALL",
)

// 커뮤니티 화면 뷰 모델
@HiltViewModel
class CommunityViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
    ) : ViewModel() {
        private val _communityState = MutableStateFlow(CommunityViewState())
        val communityState = _communityState.asStateFlow()

        init {
            viewModelScope.launch {
                loadCategories()
                loadCommunityLists()
            }
        }

        suspend fun loadCategories() {
            try {
                val result =
                    supabase
                        .from("categories")
                        .select()
                        .decodeList<CategoryModel>()

            /*
                associate  :list 형식을 map으로 변환
                ex)
                [
                CategoryModel(key="CIVIL", name="민사"),
                CategoryModel(key="CRIMINAL", name="형사"),
                ]
                    ->
                {
                  "CIVIL" : "민사",
                  "CRIMINAL" : "형사",
                  "LABOR" : "노무"
                }

//                val map = result.associate { it.key to it.name }
//                _communityState.update{
//                    it.copy(
//                        categoryList = map
//                    )
//                }
//                Log.d("카테고리", _communityState.value.categoryList.toString())

             */
                val map = linkedMapOf<String, String>()
                map["ALL"] = "전체"

                result.forEach {
                    map[it.key] = it.name
                }

                _communityState.update {
                    it.copy(categoryList = map)
                }

//                    Log.d("카테고리", "최종 맵: ${_communityState.value.categoryList}")
            } catch (e: Exception) {
                Log.e("Category Error", e.toString())
            }
        }

        suspend fun loadCommunityLists(categoryKey: String = "ALL") {
            _communityState.update { it.copy(isCommunityListLoading = true) }

            try {
                val categoryKoreanName = _communityState.value.categoryList[categoryKey]

                val result =
                    supabase
                        .from("community")
                        .select {
                            filter {
                                if (categoryKey != "ALL" && categoryKoreanName != null) {
                                    eq("category", categoryKoreanName)
                                }
                            }
                            order("created_at", order = Order.DESCENDING)
                        }.decodeList<CommunityWriteModel>()

                _communityState.update { it.copy(communityList = result) }
            } catch (e: Exception) {
                Log.e("supabase community error", e.toString())
            } finally {
                _communityState.update { it.copy(isCommunityListLoading = false) }
            }
        }

        fun onCategorySelected(categoryKey: String) {
            viewModelScope.launch {
                _communityState.update {
                    it.copy(selectedCategory = categoryKey)
                }
                loadCommunityLists(categoryKey)
            }
        }
    }
