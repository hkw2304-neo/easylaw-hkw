package com.easylaw.app.data.repository.community

import android.content.Context
import android.net.Uri
import com.easylaw.app.data.api.CommunityApiService
import com.easylaw.app.data.models.common.FileUploadModel
import com.easylaw.app.data.models.community.CommunityPrecSearchModel
import com.easylaw.app.data.models.sample.SampleResModel
import com.easylaw.app.util.Common.createMultipartBody
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class CommunityUploadRequest(
    val id: String,
    val name: String,
    val pw: String,
    val adr: String,
    val etc: String,
//    val imageUri: String? = ""
    val uploadFileList: List<FileUploadModel> = emptyList(),
)

class CommunityRepo
    @Inject
    constructor(
        private val service: CommunityApiService,
    ) {
        // 매개변수 1개
        suspend fun getCommunityLaw(query: String): CommunityPrecSearchModel = service.getCommunityLaw(query = query)

        suspend fun postCommunity(
            req: Map<String, Any>,
            context: Context,
        ): SampleResModel {
            val tempFiles = mutableListOf<MultipartBody.Part>()
            // 플러터와 달리 타입 명시해서 캐스팅해야 인식
            val filesList = req["file"] as? List<FileUploadModel> ?: emptyList()

//            예시 1.
//            val idPart = req.id.toPart()

            // 예시 2.
            // Json으로 묶어서 보내기
            // map 객체에서 특정 필드만 제외
            val textData = req.filter { it.key != "files" }
            // Json 변형
            val jsonString = Gson().toJson(textData)

            // 타입 application/json 명시
            val dataPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

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

            return service.uploadCommunity(
                data = dataPart,
                file = filePart,
            )
        }
    }
