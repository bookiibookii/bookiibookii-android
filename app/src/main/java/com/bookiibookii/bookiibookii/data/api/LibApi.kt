package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.library.BookResult
import com.bookiibookii.bookiibookii.data.model.library.BookReviewUpsertDTO
import com.bookiibookii.bookiibookii.data.model.library.GroupReviewsResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardBookmarkResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardCreateRequestDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardCreateResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardListResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MyBookReviewsResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MyGroupReviewsResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MyGroupReviewsUpdateDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleRequestDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardUpdateRequestDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberReviewCreateDTO
import com.bookiibookii.bookiibookii.data.model.library.PresignedUrlResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.PublicReadingCardResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.ShareTokenResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.TrackerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LibApi {

    // ── Library (멤버북) ───────────────────────────────────────────────────────

    @GET("api/library/memberbooks")
    suspend fun getLibraryBooks(): Response<ApiResponse<List<BookResult>>>

    @GET("api/library/memberbooks/search")
    suspend fun searchLibraryBooks(
        @Query("keyword") keyword: String
    ): Response<ApiResponse<List<BookResult>>>

    @DELETE("api/library/memberbooks/{memberBookId}")
    suspend fun deleteMemberBook(
        @Path("memberBookId") memberBookId: Int
    ): Response<ApiResponse<String>>

    // ── Cards ──────────────────────────────────────────────────────────────────

    @GET("api/member-books/group/{groupId}/cards")
    suspend fun getGroupCards(
        @Path("groupId") groupId: Int
    ): Response<ApiResponse<MemberCardListResponseDTO>>

    @GET("api/member-books/cards/detail/{cardId}")
    suspend fun getCardDetail(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<MemberCardResponseDTO>>

    @GET("api/member-books/cards/bookmarks")
    suspend fun getBookmarkedCards(): Response<ApiResponse<List<MemberCardResponseDTO>>>

    @POST("api/member-books/{memberBookId}/cards/presigned-url")
    suspend fun postPresignedUrl(
        @Path("memberBookId") memberBookId: Int
    ): Response<ApiResponse<PresignedUrlResponseDTO>>

    @POST("api/member-books/{memberBookId}/cards")
    suspend fun createCard(
        @Path("memberBookId") memberBookId: Int,
        @Body request: MemberCardCreateRequestDTO
    ): Response<ApiResponse<MemberCardCreateResponseDTO>>

    @PATCH("api/member-books/cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: Long,
        @Body request: MemberCardUpdateRequestDTO
    ): Response<ApiResponse<MemberCardResponseDTO>>

    @PATCH("api/member-books/cards/{cardId}/bookmark")
    suspend fun toggleBookmark(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<MemberCardBookmarkResponseDTO>>

    @PATCH("api/member-books/cards/{cardId}/reactions")
    suspend fun toggleReaction(
        @Path("cardId") cardId: Long,
        @Body request: MemberCardReactionToggleRequestDTO
    ): Response<ApiResponse<MemberCardReactionToggleResponseDTO>>

    @DELETE("api/member-books/cards/{cardId}")
    suspend fun deleteCard(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<String>>

    @POST("api/member-books/cards/{cardId}/share-token")
    suspend fun createShareToken(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<ShareTokenResponseDTO>>

    // 공유 토큰 기반 공개 조회 — 인증 불필요. 서버는 ApiResponse 래퍼로 감싸 반환(result에 카드)
    @GET("api/public/reading-cards/{shareToken}")
    suspend fun getPublicReadingCard(
        @Path("shareToken") shareToken: String
    ): Response<ApiResponse<PublicReadingCardResponseDTO>>

    // ── Reviews ────────────────────────────────────────────────────────────────

    @GET("api/groups/{groupId}/reviews")
    suspend fun getGroupReviews(
        @Path("groupId") groupId: Int
    ): Response<ApiResponse<GroupReviewsResponseDTO>>

    @POST("api/groups/{groupId}/reviews")
    suspend fun postBookReview(
        @Path("groupId") groupId: Int,
        @Body request: BookReviewUpsertDTO
    ): Response<ApiResponse<String>>

    @POST("api/groups/{groupId}/member-reviews")
    suspend fun postMemberReview(
        @Path("groupId") groupId: Int,
        @Body request: MemberReviewCreateDTO
    ): Response<ApiResponse<String>>

    // 내 책 리뷰 목록 조회 — 수정 시 reviewId 확보용
    @GET("api/groups/{groupId}/reviews/book/me")
    suspend fun getMyBookReviews(
        @Path("groupId") groupId: Int
    ): Response<ApiResponse<MyBookReviewsResponseDTO>>

    @PATCH("api/groups/{groupId}/reviews/book/{reviewId}")
    suspend fun updateMyReview(
        @Path("groupId") groupId: Int,
        @Path("reviewId") reviewId: Int,
        @Body request: BookReviewUpsertDTO
    ): Response<ApiResponse<String>>

    // 내 그룹 리뷰(책 리뷰 여러 개 + 파트너 리뷰) 일괄 수정
    @PATCH("api/groups/{groupId}/reviews/my-group")
    suspend fun updateMyGroupReviews(
        @Path("groupId") groupId: Int,
        @Body request: MyGroupReviewsUpdateDTO
    ): Response<ApiResponse<MyGroupReviewsResponseDTO>>

    // ── Trackers ───────────────────────────────────────────────────────────────

    @GET("api/me/trackers")
    suspend fun getMyTrackers(): Response<TrackerResponse>
}
