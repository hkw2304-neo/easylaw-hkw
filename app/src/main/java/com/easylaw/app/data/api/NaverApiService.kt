package com.easylaw.app.data.api

import com.easylaw.app.data.models.naver.NaverNewsModel
import retrofit2.http.GET
import retrofit2.http.QueryMap

/*
    fun 해당모델.toMap(): Map<String, String> = mapOf(
        "query" to query,
        "display" to display.toString(),
        "sort" to sort
    )
  이렇게 해야 기본값 설정 가능
 */
data class NaverNewsReqModel(
    val query: String,
    val display: Int = 10, // 기본 10개
    val sort: String = "sim", // 유사도순(sim) 또는 날짜순(date)
) {
    fun toMap(): Map<String, String> =
        mapOf(
            "query" to query,
            "display" to display.toString(),
            "sort" to sort,
        )
}

interface NaverApiService {
    @GET("v1/search/news.json")
    suspend fun getNaverNews(
//        @Query("query") query: String,
//        @Query("display") display: Int = 10, // 기본 10개
//        @Query("sort") sort: String = "sim", // 유사도순(sim) 또는 날짜순(date)
        // 왠만하면 파라미터는 굉장히 많을거다 그래서 모델을 만들어서 보내는게 나을듯?
        // retrofit은 객체는 인식 못하지만 map은 인식한다.
        @QueryMap params: Map<String, String>,
    ): NaverNewsModel
}
