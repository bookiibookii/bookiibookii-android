package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.CardDetailResponse
import com.bookiibookii.bookiibookii.data.model.CardOperationResponse
import com.bookiibookii.bookiibookii.data.model.CommentListResponse
import com.bookiibookii.bookiibookii.data.model.CreateCardRequest
import com.bookiibookii.bookiibookii.data.model.CommonResponse
import com.bookiibookii.bookiibookii.data.model.BookSearchResponse
import com.bookiibookii.bookiibookii.data.model.CardListResponse
import com.bookiibookii.bookiibookii.data.model.GroupCreateRequest
import com.bookiibookii.bookiibookii.data.model.GroupCreateResponse
import com.bookiibookii.bookiibookii.data.model.GroupItemDto
import com.bookiibookii.bookiibookii.data.model.GroupListResponse
import com.bookiibookii.bookiibookii.data.model.GroupMemberResponse
import com.bookiibookii.bookiibookii.data.model.InquiryCreateResponse
import com.bookiibookii.bookiibookii.data.model.InquiryListResponse
import com.bookiibookii.bookiibookii.data.model.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.LibraryResponse
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
import com.bookiibookii.bookiibookii.data.model.ReviewRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshResponse
import com.bookiibookii.bookiibookii.data.model.UpdateCardRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateResponse
import com.bookiibookii.bookiibookii.data.model.WithdrawResponse
import okhttp3.RequestBody
import com.bookiibookii.bookiibookii.trkData.api.TrkApi
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url
import retrofit2.http.Query

interface ApiService: TrkApi {

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

    // 프로필 수정
//    @POST("api/users/name-validation")
//    suspend fun checkNickname(
//        @Body request: NicknameCheckRequest
//    ): Response<NicknameCheckResponse>

    @POST("api/users/me/image/presigend-url")
    suspend fun getPresignedUrl(): Response<PresignedUrlResponse>

    @PUT
    suspend fun uploadImageToS3(
        @Url url: String,
        @Body image: RequestBody
    ): Response<Unit>

    @GET("api/library/books")
    suspend fun getLibraryBooks() : Response<LibraryResponse>

    @GET("api/card/detail/{cardId}")
    suspend fun getCardDetail(
        @Path("cardId") cardId: Long
    ): Response<CardDetailResponse>

    // 댓글 목록 조회
    @GET("api/cards/{cardId}/comments")
    suspend fun getCardComments(
        @Path("cardId") cardId: Long
    ): Response<CommentListResponse>

    //  Presigned URL 발급
    @POST("api/card/{userBookId}/presigned-url")
    suspend fun getPresignedUrl(
        @Path("userBookId") userBookId: Int
    ): Response<PresignedUrlResponse>

    // 독서카드 목록 조회
    @GET("api/card/{userBookId}")
    suspend fun getBookCards(
        @Path("userBookId") userBookId: Int
    ): Response<CardListResponse>

    @POST("api/reviews/books/{userBookId}")
    suspend fun postBookReview(
        @Path("userBookId") userBookId: Int,
        @Body request: ReviewRequest
    ): Response<com.bookiibookii.bookiibookii.data.model.BaseResponse>

    //  카드 생성
    @POST("api/card/{userBookId}")
    suspend fun createCard(
        @Path("userBookId") userBookId: Int,
        @Body request: CreateCardRequest
    ): Response<CardOperationResponse>

    // 카드 수정
    @PATCH("api/card/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: Long,
        @Body request: UpdateCardRequest
    ): Response<CardOperationResponse>


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

    // 인기 검색어
    @GET("api/groups/popular-keywords")
    suspend fun getPopularKeywords(): Response<GroupItemDto.PopularSearchResponse>

    @GET("/api/groups/search")
    suspend fun searchGroups(
        @Query("keyword") keyword: String,
        @Query("sort") sort: String = "latest" // 정렬 옵션 (필요시)
    ): Response<GroupItemDto.GroupSearchResponse>

    // 그룹 상세 조회
    @GET("api/groups/{groupId}")
    suspend fun getGroupDetail(
        @Path("groupId") groupId: Int
    ): Response<GroupItemDto.GroupDetailResponse>

    //그룹 신청하기
    @POST("api/groups/{groupId}/apply")
    suspend fun applyGroup(
        @Path("groupId") groupId: Long,
        @Body request: GroupItemDto.GroupApplyRequest
    ): Response<GroupItemDto.GroupApplyResponse>

    //그룹신청 취소하기
    @DELETE("api/groups/{groupId}/apply")
    suspend fun cancelGroupApplication(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.GroupCancelResponse>

    //그룹 신청 조회
    @GET("api/groups/{groupId}/applylist")
    suspend fun getGroupApplications(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.GroupAppListResponse>

    @PATCH("api/groups/apply/{applyId}")
    suspend fun updateApplicationStatus(
        @Path("applyId") applyId: Long,
        @Body request: GroupItemDto.GroupAppStatusRequest
    ): Response<GroupItemDto.GroupAppStatusResponse>
}