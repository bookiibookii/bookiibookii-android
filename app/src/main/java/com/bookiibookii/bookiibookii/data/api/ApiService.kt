package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.BookSearchResponse
import com.bookiibookii.bookiibookii.data.model.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.GroupCreateResponse
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.data.model.GroupListResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService : TrkApi, MypApi, LibApi, AuthApi, NotiApi, UserApi, RecmApi, KwdApi {

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
}