package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.recommendation.RecommendedBookmateItem
import com.bookiibookii.bookiibookii.data.model.recommendation.RecommendedGroupItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RecmApi {

    // 홈 추천 그룹
    @GET("/api/recommendations/groups")
    suspend fun getRecommendedGroups(
        @Query("refresh") refresh: Boolean = false
    ): Response<ApiResponse<List<RecommendedGroupItem>>>

    // 부키메이트 추천
    @GET("/api/recommendations/bookmates")
    suspend fun getRecommendedBookmates(): Response<ApiResponse<List<RecommendedBookmateItem>>>
}