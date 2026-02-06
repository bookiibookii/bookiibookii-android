package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.CommonResponse
import com.bookiibookii.bookiibookii.data.model.BookSearchResponse
import com.bookiibookii.bookiibookii.data.model.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.GroupCreateResponse
import com.bookiibookii.bookiibookii.data.model.GroupListRequest
import com.bookiibookii.bookiibookii.data.model.GroupListResponse
import com.bookiibookii.bookiibookii.data.model.GroupMemberResponse
import com.bookiibookii.bookiibookii.data.model.InquiryCreateResponse
import com.bookiibookii.bookiibookii.data.model.InquiryListResponse
import com.bookiibookii.bookiibookii.data.model.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.LoginRequest
import com.bookiibookii.bookiibookii.data.model.LoginResponse
import com.bookiibookii.bookiibookii.data.model.LogoutResponse
import com.bookiibookii.bookiibookii.data.model.MyGroupResponse
import com.bookiibookii.bookiibookii.data.model.MypageResponse
import com.bookiibookii.bookiibookii.data.model.NicknameValidationResponse
import com.bookiibookii.bookiibookii.data.model.NoticeDetailResponse
import com.bookiibookii.bookiibookii.data.model.NoticeListResponse
import com.bookiibookii.bookiibookii.data.model.OnboardingRequest
import com.bookiibookii.bookiibookii.data.model.PresignedUrlResponse
import com.bookiibookii.bookiibookii.data.model.ReportCreateResponse
import com.bookiibookii.bookiibookii.data.model.ReportListResponse
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshResponse
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateResponse
import com.bookiibookii.bookiibookii.data.model.WithdrawResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun postLogin(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/mypage")
    suspend fun getMypage(): Response<MypageResponse>

    @PATCH("api/mypage")
    suspend fun updateProfile(
        @Body request: UserUpdateRequest
    ): Response<UserUpdateResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @DELETE("api/auth/withdraw")
    suspend fun withdraw(): Response<WithdrawResponse>

    @GET("api/notice")
    suspend fun getNoticeList(): Response<NoticeListResponse>

    @GET("api/notice/{noticeId}")
    suspend fun getNoticeDetail(
        @Path("noticeId") noticeId: Int
    ): Response<NoticeDetailResponse>

    @GET("api/inquiry")
    suspend fun getInquiryList(): Response<InquiryListResponse>

    @POST("api/inquiry")
    suspend fun postInquiry(@Body request: InquiryRequest): Response<InquiryCreateResponse>

    @GET("api/report")
    suspend fun getReportList(): Response<ReportListResponse>

    @POST("api/report")
    suspend fun postReport(@Body request: ReportRequest): Response<ReportCreateResponse>

    @POST("api/auth/refresh")
    fun refreshToken(
        @Body request: TokenRefreshRequest
    ): retrofit2.Call<TokenRefreshResponse>

    @GET("api/groups/my")
    suspend fun getMyGroups(): Response<MyGroupResponse>

    @GET("api/groups/{groupId}/members")
    suspend fun getGroupMembers(
        @Path("groupId") groupId: Int
    ): Response<GroupMemberResponse>

    // 닉네임 중복 검증
    @POST("api/users/name-validation")
    suspend fun postNicknameValidation(
        @Query("nickname") nickname: String
    ): Response<NicknameValidationResponse>

    // 사용자 이미지 업로드용 Presigned URL 발급
    @POST("api/users/me/image/presigned-url")
    suspend fun postPresignedUrl(): Response<PresignedUrlResponse>

    // 온보딩
    @POST("/api/onboarding")
    suspend fun postOnboarding(
        @Body body: OnboardingRequest
    ): Response<CommonResponse<String>>

    // 그룹 생성
    @POST("api/groups")
    suspend fun createGroup(
        @Body request: GroupCreateRequest
    ): Response<GroupCreateResponse>

    // 도서 검색
    @GET("api/books/search")
    suspend fun searchBooks(
        @Query("keyword") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): Response<BookSearchResponse>

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
}