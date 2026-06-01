package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.library.BookResult
import com.bookiibookii.bookiibookii.data.model.library.BookReviewUpsertDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardBookmarkResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardCreateRequestDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardCreateResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardListResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleRequestDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardReactionToggleResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardResponseDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberCardUpdateRequestDTO
import com.bookiibookii.bookiibookii.data.model.library.MemberReviewCreateDTO
import com.bookiibookii.bookiibookii.data.model.library.PresignedUrlResponseDTO
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

    // ── Reviews ────────────────────────────────────────────────────────────────

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

    @PATCH("api/groups/{groupId}/reviews/me")
    suspend fun updateMyReview(
        @Path("groupId") groupId: Int,
        @Body request: BookReviewUpsertDTO
    ): Response<ApiResponse<String>>

    // ── Trackers ───────────────────────────────────────────────────────────────

    @GET("api/me/trackers")
    suspend fun getMyTrackers(): Response<TrackerResponse>
}
