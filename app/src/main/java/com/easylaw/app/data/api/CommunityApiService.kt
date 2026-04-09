package com.easylaw.app.data.api

import com.easylaw.app.data.models.community.CommunityPrecSearchModel
import retrofit2.http.GET
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

    /*
    post
    http의 body라는 보이지 않는 곳에 담아서 보낸다.

    @Multipart
    @POST("sample/upload")
    suspend fun uploadCommunityWithFile(
         - 같이 보낼 파라미터가 있으면 추가
        RequestBody 정석, 자동으로 문자열로 반환
        @Part("id") id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("pw") pw: RequestBody,
        @Part("adr") adr: RequestBody,
        @Part("etc") etc: RequestBody,
        @Part file: MultipartBody.Part <- 멀티파트로 변환하면서 자동으로 키를 달아준다.
    ) : SampleReqModel

    @Multipart
    @POST("sample/upload")
    suspend fun uploadCommunity(
         - 같이 보낼 파라미터가 있으면 추가
        RequestBody 정석, 자동으로 문자열로 반환
        @Part("id") id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("pw") pw: RequestBody,
        @Part("adr") adr: RequestBody,
        @Part("etc") etc: RequestBody,
    ) : SampleReqModel


     */
}
