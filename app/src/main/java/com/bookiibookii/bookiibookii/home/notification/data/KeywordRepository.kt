package com.bookiibookii.bookiibookii.home.notification.data

import com.bookiibookii.bookiibookii.data.api.ApiService
import com.bookiibookii.bookiibookii.data.model.KeywordCreateRequest
import com.bookiibookii.bookiibookii.data.model.KeywordCreateResultDto
import com.bookiibookii.bookiibookii.data.model.KeywordListResultDto
import com.bookiibookii.bookiibookii.trkData.dto.ApiResponse
import retrofit2.Response

class KeywordRepository(
    private val api: ApiService
) {
    suspend fun fetchKeywords(sort: String): Response<ApiResponse<KeywordListResultDto>> {
        return api.getKeywords(sort = sort)
    }

    suspend fun addKeyword(content: String): Response<ApiResponse<KeywordCreateResultDto>> {
        return api.createKeyword(KeywordCreateRequest(content = content))
    }

    suspend fun removeKeyword(keywordId: Long): Response<ApiResponse<String>> {
        return api.deleteKeyword(keywordId)
    }
}