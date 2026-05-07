package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.BookSearchResponse
import com.bookiibookii.bookiibookii.data.model.CommonResponse
import com.bookiibookii.bookiibookii.data.model.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.GroupCreateResponse
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.data.model.GroupListResponse
import com.bookiibookii.bookiibookii.data.model.LoginRequest
import com.bookiibookii.bookiibookii.data.model.LoginResponse
import com.bookiibookii.bookiibookii.data.model.NotificationItemDto
import com.bookiibookii.bookiibookii.data.model.NotificationListResultDto
import com.bookiibookii.bookiibookii.data.model.OnboardingRequest
import com.bookiibookii.bookiibookii.data.model.RecommendedBookmateDto
import com.bookiibookii.bookiibookii.data.model.RecommendedGroupDto
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService : TrkApi, MypApi, LibApi {

    // 로그인
    @POST("api/auth/login")
    suspend fun postLogin(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // 토큰 갱신
    @POST("api/auth/refresh")
    suspend fun postRefresh(
        @Header("Authorization") authorization: String,
        @Body request: TokenRefreshRequest
    ): Response<TokenRefreshResponse>

    // 온보딩
    @POST("/api/onboarding")
    suspend fun postOnboarding(
        @Body body: OnboardingRequest
    ): Response<CommonResponse<String>>

    // 도서 검색
    @GET("api/books/search")
    suspend fun searchBooks(
        @Query("keyword") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<BookSearchResponse>

    // 그룹 생성
    @POST("api/groups")
    suspend fun createGroup(
        @Body request: GroupCreateRequest
    ): Response<GroupCreateResponse>

    // 그룹 목록 조회
    @GET("/api/groups")
    suspend fun getGroupList(
        @Query("groupTypes") groupTypes: List<String>?,
        @Query("tradeTypes") tradeTypes: List<String>?,
        @Query("meetPlace") meetPlace: List<String>?,
        @Query("categories") categories: List<String>?,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<GroupListResponse>

    // 인기 검색어
    @GET("api/groups/popular-keywords")
    suspend fun getPopularKeywords(): Response<GroupItemDto.PopularSearchResponse>

    // 그룹 검색
    @GET("/api/groups/search")
    suspend fun searchGroups(
        @Query("keyword") keyword: String,
        @Query("sort") sort: String = "latest"
    ): Response<GroupItemDto.GroupSearchResponse>

    // 그룹 상세 조회
    @GET("api/groups/{groupId}")
    suspend fun getGroupDetail(
        @Path("groupId") groupId: Int
    ): Response<GroupItemDto.GroupDetailResponse>

    // 그룹 신청하기
    @POST("api/groups/{groupId}/apply")
    suspend fun applyGroup(
        @Path("groupId") groupId: Long,
        @Body request: GroupItemDto.GroupApplyRequest
    ): Response<GroupItemDto.GroupApplyResponse>

    // 그룹 신청 취소하기
    @DELETE("api/groups/{groupId}/apply")
    suspend fun cancelGroupApplication(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.GroupCancelResponse>

    // 그룹 신청 목록 조회
    @GET("api/groups/{groupId}/applylist")
    suspend fun getGroupApplications(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.GroupAppListResponse>

    // 그룹 참여 요청 수락/거절
    @PATCH("api/groups/apply/{applyId}")
    suspend fun updateApplicationStatus(
        @Path("applyId") applyId: Long,
        @Body request: GroupItemDto.GroupAppStatusRequest
    ): Response<GroupItemDto.GroupAppStatusResponse>

    // 그룹 삭제하기
    @DELETE("api/groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.GroupDeleteResponse>

    // 그룹 수정하기
    @PATCH("api/groups/{groupId}")
    suspend fun modifyGroup(
        @Path("groupId") groupId: Long,
        @Body request: GroupItemDto.GroupModifyRequest
    ): Response<GroupItemDto.GroupModifyResponse>

    // 그룹 댓글 달기
    @POST("api/groups/{groupId}/comments")
    suspend fun postComment(
        @Path("groupId") groupId: Long,
        @Body request: GroupItemDto.CommentCreateRequest
    ): Response<GroupItemDto.CommentCreateResponse>

    // 그룹 댓글 조회
    @GET("api/groups/{groupId}/comments")
    suspend fun getComments(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.CommentListResponse>

    // 그룹 댓글 삭제
    @DELETE("api/groups/{groupId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("groupId") groupId: Int,
        @Path("commentId") commentId: Int
    ): Response<GroupItemDto.CommentDeleteResponse>

    // 알림 목록 조회
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("category") category: String,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int
    ): Response<com.bookiibookii.bookiibookii.data.model.common.ApiResponse<NotificationListResultDto>>

    // 알림 읽음 처리
    @PATCH("api/notifications/{notificationId}/read")
    suspend fun readNotification(
        @Path("notificationId") notificationId: Long
    ): Response<com.bookiibookii.bookiibookii.data.model.common.ApiResponse<NotificationItemDto>>

    // 키워드 조회
    @GET("/api/keywords")
    suspend fun getKeywords(
        @Query("sort") sort: String
    ): Response<com.bookiibookii.bookiibookii.data.model.common.ApiResponse<com.bookiibookii.bookiibookii.data.model.KeywordListResultDto>>

    // 키워드 생성
    @POST("/api/keywords")
    suspend fun createKeyword(
        @Body request: com.bookiibookii.bookiibookii.data.model.KeywordCreateRequest
    ): Response<com.bookiibookii.bookiibookii.data.model.common.ApiResponse<com.bookiibookii.bookiibookii.data.model.KeywordCreateResultDto>>

    // 키워드 삭제
    @DELETE("/api/keywords/{keywordId}")
    suspend fun deleteKeyword(
        @Path("keywordId") keywordId: Long
    ): Response<com.bookiibookii.bookiibookii.data.model.common.ApiResponse<String>>

    // 홈 추천 그룹
    @GET("/api/recommendations/groups")
    suspend fun getRecommendedGroups(
        @Query("refresh") refresh: Boolean = false
    ): Response<CommonResponse<List<RecommendedGroupDto>>>

    // 부키메이트 추천
    @GET("/api/recommendations/bookmates")
    suspend fun getRecommendedBookmates(): Response<CommonResponse<List<RecommendedBookmateDto>>>
}
