package com.easylaw.app.data.repository.community

import com.easylaw.app.data.api.CommunityApiService
import com.easylaw.app.data.models.community.CommunityPrecSearchModel
import javax.inject.Inject

class CommunityRepo
    @Inject
    constructor(
        private val service: CommunityApiService,
    ) {
        suspend fun getCommunityLaw(query: String): CommunityPrecSearchModel = service.getCommunityLaw(query = query)
    }
