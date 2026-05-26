package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.group.BookSearchResponse
import com.bookiibookii.bookiibookii.data.model.group.CommentCreateRequest
import com.bookiibookii.bookiibookii.data.model.group.CommentCreateResponse
import com.bookiibookii.bookiibookii.data.model.group.CommentItem
import com.bookiibookii.bookiibookii.data.model.group.GroupAppListResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupAppStatusRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupAppStatusResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupApplyRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupApplyResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupCancelResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupCreateResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupDeleteResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupDetailResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupListResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupModifyRequest
import com.bookiibookii.bookiibookii.data.model.group.GroupModifyResponse
import com.bookiibookii.bookiibookii.data.model.group.GroupSearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GrpApi {

    // 도서 검색
    @GET("api/books/search")
    suspend fun searchBooks(
        @Query("keyword") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<BookSearchResponse>>

    // 그룹 생성
    @POST("api/groups")
    suspend fun createGroup(
        @Body request: GroupCreateRequest
    ): Response<ApiResponse<GroupCreateResponse>>

    // 그룹 목록 조회
    @GET("/api/groups")
    suspend fun getGroupList(
        @Query("tradeTypes") tradeTypes: List<String>?,
        @Query("regions") regions: List<String>?,
        @Query("categories") categories: List<String>?,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<GroupListResponse>>

    // 인기 검색어
    @GET("api/groups/popular-keywords")
    suspend fun getPopularKeywords(): Response<ApiResponse<List<String>>>

    // 그룹 검색
    @GET("/api/groups/search")
    suspend fun searchGroups(
        @Query("keyword") keyword: String,
        @Query("sort") sort: String = "LATEST",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ApiResponse<GroupSearchResponse>>

    // 그룹 상세 조회
    @GET("api/groups/{groupId}")
    suspend fun getGroupDetail(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<GroupDetailResponse>>

    // 그룹 신청하기
    @POST("api/groups/{groupId}/apply")
    suspend fun applyGroup(
        @Path("groupId") groupId: Long,
        @Body request: GroupApplyRequest
    ): Response<ApiResponse<GroupApplyResponse>>

    // 그룹 신청 취소하기
    @DELETE("api/groups/{groupId}/apply")
    suspend fun cancelGroupApplication(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<GroupCancelResponse>>

    // 그룹 신청 목록 조회
    @GET("api/groups/{groupId}/applylist")
    suspend fun getGroupApplications(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<GroupAppListResponse>>

    // 그룹 참여 요청 수락/거절
    @PATCH("api/groups/apply/{applyId}")
    suspend fun updateApplicationStatus(
        @Path("applyId") applyId: Long,
        @Body request: GroupAppStatusRequest
    ): Response<ApiResponse<GroupAppStatusResponse>>

    // 그룹 삭제하기
    @DELETE("api/groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<GroupDeleteResponse>>

    // 그룹 수정하기
    @PATCH("api/groups/{groupId}")
    suspend fun modifyGroup(
        @Path("groupId") groupId: Long,
        @Body request: GroupModifyRequest
    ): Response<ApiResponse<GroupModifyResponse>>

    // 그룹 댓글 달기
    @POST("api/groups/{groupId}/comments")
    suspend fun postComment(
        @Path("groupId") groupId: Long,
        @Body request: CommentCreateRequest
    ): Response<ApiResponse<CommentCreateResponse>>

    // 그룹 댓글 조회
    @GET("api/groups/{groupId}/comments")
    suspend fun getComments(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<List<CommentItem>>>

    // 그룹 댓글 삭제
    @DELETE("api/groups/{groupId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("groupId") groupId: Long,
        @Path("commentId") commentId: Long
    ): Response<ApiResponse<String>>
}
