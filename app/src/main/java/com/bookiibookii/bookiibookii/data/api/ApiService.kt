package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.BaseResponse
import com.bookiibookii.bookiibookii.data.model.CardDetailResponse
import com.bookiibookii.bookiibookii.data.model.CardOperationResponse
import com.bookiibookii.bookiibookii.data.model.CommentListResponse
import com.bookiibookii.bookiibookii.data.model.CreateCardRequest
import com.bookiibookii.bookiibookii.data.model.CommonResponse
import com.bookiibookii.bookiibookii.data.model.BookSearchResponse
import com.bookiibookii.bookiibookii.data.model.BookmarkListResponse
import com.bookiibookii.bookiibookii.data.model.BookmarkToggleResponse
import com.bookiibookii.bookiibookii.data.model.CompleteReadingResponse
import com.bookiibookii.bookiibookii.data.model.CreateCardResponse
import com.bookiibookii.bookiibookii.data.model.GroupCardListResponse
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
import com.bookiibookii.bookiibookii.data.model.NotificationItemDto
import com.bookiibookii.bookiibookii.data.model.NotificationListResultDto
import com.bookiibookii.bookiibookii.data.model.OnboardingRequest
import com.bookiibookii.bookiibookii.data.model.PostCommentRequest
import com.bookiibookii.bookiibookii.data.model.PostCommentResponse
import com.bookiibookii.bookiibookii.data.model.PresignedUrlResponse
import com.bookiibookii.bookiibookii.data.model.ProfileResponse
import com.bookiibookii.bookiibookii.data.model.RecommendedBookmateDto
import com.bookiibookii.bookiibookii.data.model.RecommendedGroupDto
import com.bookiibookii.bookiibookii.data.model.ReportCreateResponse
import com.bookiibookii.bookiibookii.data.model.ReportListResponse
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.data.model.ReviewRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshRequest
import com.bookiibookii.bookiibookii.data.model.TokenRefreshResponse
import com.bookiibookii.bookiibookii.data.model.TrackerResponse
import com.bookiibookii.bookiibookii.data.model.UpdateCardRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateResponse
import com.bookiibookii.bookiibookii.data.model.WithdrawResponse
import com.bookiibookii.bookiibookii.onboarding.login.LoginActivity
import okhttp3.RequestBody
import com.bookiibookii.bookiibookii.trkData.api.TrkApi
import com.bookiibookii.bookiibookii.trkData.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
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

    @POST("/api/auth/refresh")
    suspend fun postRefresh(
        @Body request: TokenRefreshRequest
    ): Response<TokenRefreshResponse>

    @GET("api/report/groups/my")
    suspend fun getMyGroups(): Response<MyGroupResponse>

    @GET("api/report/{groupId}/members")
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

    @GET("api/cards/detail/{cardId}")
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

    // 독서카드 그룹 조회
    @GET("api/cards/group/{groupId}")
    suspend fun getGroupCards(
        @Path("groupId") groupId: Int
    ): Response<GroupCardListResponse>

    // 리뷰 작성
    @POST("api/reviews/together/{userBookId}")
    suspend fun postBookReview(
        @Path("userBookId") userBookId: Int,
        @Body request: ReviewRequest
    ): Response<BaseResponse> // BaseResponse는 result가 String인 공통 응답


    // 댓글 작성
    @POST("api/cards/{cardId}/comments")
    suspend fun postCardComment(
        @Path("cardId") cardId: Long,
        @Body request: PostCommentRequest
    ): Response<PostCommentResponse>

    // Presigned URL 발급 (카드 생성 전용)
    @POST("api/cards/{userBookId}/presigned-url")
    suspend fun postPresignedUrl(
        @Path("userBookId") userBookId: Int
    ): Response<PresignedUrlResponse>

    // 독서카드 생성
    @POST("api/cards/{userBookId}")
    suspend fun createCard(
        @Path("userBookId") userBookId: Int,
        @Body request: CreateCardRequest
    ): Response<CreateCardResponse>

    // 카드 수정 (기존 유지, 추후 연결)
    @PATCH("api/cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: Long,
        @Body request: UpdateCardRequest
    ): Response<CardOperationResponse>

    // 북마크 토글
    @PATCH("api/cards/{cardId}/bookmark")
    suspend fun toggleBookmark(
        @Path("cardId") cardId: Long
    ): Response<BookmarkToggleResponse>

    // 북마크 목록 조회
    @GET("api/cards/bookmarks")
    suspend fun getBookmarkedCards(): Response<BookmarkListResponse>

    @DELETE("api/library/{userBookId}")
    suspend fun deleteGroup(
        @Path("userBookId") userBookId: Int
    ): Response<BaseResponse>

    @GET("/api/profiles/{nickname}")
    suspend fun getUserProfile(
        @Path("nickname") nickname: String
    ): Response<LoginActivity.ProfileResponse> // ProfileResponse는 MypageResult를 감싸는 형태여야 함

    @GET("/api/groups/me/trackers")
    suspend fun getMyTrackers(): Response<TrackerResponse>

    @PATCH("/api/groups/{groupId}/together/members/me/complete")
    suspend fun completeReading(
        @Path("groupId") groupId: Int
    ): Response<CompleteReadingResponse>

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

    //그룹 참여 요청 수락/거절
    @PATCH("api/groups/apply/{applyId}")
    suspend fun updateApplicationStatus(
        @Path("applyId") applyId: Long,
        @Body request: GroupItemDto.GroupAppStatusRequest
    ): Response<GroupItemDto.GroupAppStatusResponse>

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("category") category: String,
        @Query("cursor") cursor: String?,
        @Query("size") size: Int
    ): Response<com.bookiibookii.bookiibookii.trkData.dto.ApiResponse<NotificationListResultDto>>

    @PATCH("api/notifications/{notificationId}/read")
    suspend fun readNotification(
        @Path("notificationId") notificationId: Long
    ): Response<com.bookiibookii.bookiibookii.trkData.dto.ApiResponse<NotificationItemDto>>

    @GET("/api/keywords")
    suspend fun getKeywords(
        @Query("sort") sort: String // "LATEST" | "ALPHABETICAL"
    ): Response<com.bookiibookii.bookiibookii.trkData.dto.ApiResponse<com.bookiibookii.bookiibookii.data.model.KeywordListResultDto>>

    @POST("/api/keywords")
    suspend fun createKeyword(
        @Body request: com.bookiibookii.bookiibookii.data.model.KeywordCreateRequest
    ): Response<com.bookiibookii.bookiibookii.trkData.dto.ApiResponse<com.bookiibookii.bookiibookii.data.model.KeywordCreateResultDto>>

    @DELETE("/api/keywords/{keywordId}")
    suspend fun deleteKeyword(
        @Path("keywordId") keywordId: Long
    ): Response<com.bookiibookii.bookiibookii.trkData.dto.ApiResponse<String>>
  
    @DELETE("api/cards/{cardId}")
    suspend fun deleteCard(
        @Path("cardId") cardId: Long
    ): Response<BaseResponse>

    //그룹 삭제하기
    @DELETE("api/groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.GroupDeleteResponse>

    //그룹 수정하기
    @PATCH("api/groups/{groupId}")
    suspend fun modifyGroup(
        @Path("groupId") groupId: Long,
        @Body request: GroupItemDto.GroupModifyRequest
    ): Response<GroupItemDto.GroupModifyResponse>

    //그룹 댓글달기
    @POST("api/groups/{groupId}/comments")
    suspend fun postComment(
        @Path("groupId") groupId: Long,
        @Body request: GroupItemDto.CommentCreateRequest
    ): Response<GroupItemDto.CommentCreateResponse>

    //그룹 댓글 조회
    @GET("api/groups/{groupId}/comments")
    suspend fun getComments(
        @Path("groupId") groupId: Long
    ): Response<GroupItemDto.CommentListResponse>

    // 홈 추천 그룹 (3개)
    @GET("/api/recommendations/groups")
    suspend fun getRecommendedGroups(
        @Query("refresh") refresh: Boolean = false
    ): Response<CommonResponse<List<RecommendedGroupDto>>>

    // 부키메이트 추천 (최대 5명)
    @GET("/api/recommendations/bookmates")
    suspend fun getRecommendedBookmates(): Response<CommonResponse<List<RecommendedBookmateDto>>>
}