package com.easylaw.app.data.repository.community

import com.easylaw.app.data.api.CommunityApiService
import com.easylaw.app.data.models.community.CommunityPrecSearchModel
import javax.inject.Inject

// data class CommunityUploadRequest(
//    val id: String,
//    val name: String,
//    val pw: String,
//    val adr: String,
//    val etc: String,
//    val imageUri: Uri?
// )

class CommunityRepo
    @Inject
    constructor(
        private val service: CommunityApiService,
    ) {
        // 매개변수 1개
        suspend fun getCommunityLaw(query: String): CommunityPrecSearchModel = service.getCommunityLaw(query = query)
//        suspend fun postCommunity(request: CommunityUploadRequest): SampleReqModel{
//            val idBody = request.id.toRequestBody("text/plain".toMediaTypeOrNull())
//            val nameBody = request.name.toRequestBody("text/plain".toMediaTypeOrNull())
//
//            val filePart = request.imageUri?.let { createMultipartBody(it) }
//
//            return service.uploadFile(idBody, nameBody, ..., filePart)
//        }
    }
