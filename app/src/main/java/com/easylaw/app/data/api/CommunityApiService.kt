package com.easylaw.app.data.api

import com.easylaw.app.data.models.community.CommunityPrecSearchModel
import com.easylaw.app.data.models.sample.SampleReqModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface CommunityApiService {
    @GET("DRF/lawSearch.do")
    suspend fun getCommunityLaw(
        @Query("OC") OC: String = "rhrnak2304",
        @Query("target") target: String = "prec",
        @Query("type") type: String = "JSON",
        @Query("search") search: String = "2",
        @Query("query") query: String,
    ): CommunityPrecSearchModel

    // 업로드의 경우 특정 반환값보단 성공 여부만 확인, Response<Unit>

    // 1. 멑티 파트로 보낼 경우
    // 파라미터에 RequestBody형태로 보낸다.
    @Multipart
    @POST("sample/upload")
    suspend fun uploadCommunity(
        @Part("data") data: RequestBody,
//        @Part("id") id: RequestBody,
//        @Part("name") name: RequestBody,
//        @Part("pw") pw: RequestBody,
//        @Part("adr") adr: RequestBody,
//        @Part("etc") etc: RequestBody,
        //  멀티파트로 변환하면서 자동으로 키를 달아준다.(file <- 사용자 정의)
        @Part file: List<MultipartBody.Part>? = null,
    ): Response<Unit>

    // 2. 텍스트만 보낼 경우
    @POST("sample/upload")
    suspend fun uploadCommunityOnlyText(
        @Body data: SampleReqModel,
    ): Response<Unit>
}
