package com.bookiibookii.bookiibookii.home.notification.data

import com.bookiibookii.bookiibookii.data.api.KwdApi
import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordCreateRequest
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordCreateResult
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordListResult
import retrofit2.Response

class KeywordRepository(
    private val api: KwdApi
) {
    suspend fun fetchKeywords(sort: String): Response<ApiResponse<KeywordListResult>> {
        return api.getKeywords(sort = sort)
    }

    suspend fun addKeyword(content: String): Response<ApiResponse<KeywordCreateResult>> {
        return api.createKeyword(KeywordCreateRequest(content = content))
    }

    suspend fun removeKeyword(keywordId: Long): Response<ApiResponse<String>> {
        return api.deleteKeyword(keywordId)
    }
}
