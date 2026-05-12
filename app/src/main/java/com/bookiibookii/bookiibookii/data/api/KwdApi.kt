package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordCreateRequest
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordCreateResult
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordItem
import com.bookiibookii.bookiibookii.data.model.keyword.KeywordListResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KwdApi {

    // 키워드 조회
    @GET("/api/keywords")
    suspend fun getKeywords(
        @Query("sort") sort: String
    ): Response<ApiResponse<KeywordListResult>>

    // 키워드 등록
    @POST("/api/keywords")
    suspend fun createKeyword(
        @Body request: KeywordCreateRequest
    ): Response<ApiResponse<KeywordCreateResult>>

    // 키워드 삭제
    @DELETE("/api/keywords/{keywordId}")
    suspend fun deleteKeyword(
        @Path("keywordId") keywordId: Long
    ): Response<ApiResponse<String>>
}