package com.easylaw.app.data.repository.community

import com.easylaw.app.data.api.NaverApiService
import com.easylaw.app.data.models.naver.NaverNewsModel
import javax.inject.Inject

class NaverNewsRepo
    @Inject
    constructor(
        private val service: NaverApiService,
    ) {
        suspend fun getNaverNews(query: String): NaverNewsModel = service.getNaverNews(query)
    }
