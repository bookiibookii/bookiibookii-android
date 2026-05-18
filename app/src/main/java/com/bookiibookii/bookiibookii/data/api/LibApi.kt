package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.library.BookmarkListResponse
import com.bookiibookii.bookiibookii.data.model.library.BookmarkToggleResponse
import com.bookiibookii.bookiibookii.data.model.library.CardDetailResponse
import com.bookiibookii.bookiibookii.data.model.library.CardOperationResponse
import com.bookiibookii.bookiibookii.data.model.library.CommentListResponse
import com.bookiibookii.bookiibookii.data.model.library.CompleteReadingResponse
import com.bookiibookii.bookiibookii.data.model.library.CreateCardRequest
import com.bookiibookii.bookiibookii.data.model.library.CreateCardResponse
import com.bookiibookii.bookiibookii.data.model.library.GroupCardListResponse
import com.bookiibookii.bookiibookii.data.model.library.LibraryResponse
import com.bookiibookii.bookiibookii.data.model.library.PostCommentRequest
import com.bookiibookii.bookiibookii.data.model.library.PostCommentResponse
import com.bookiibookii.bookiibookii.data.model.library.RelayBookReviewRequest
import com.bookiibookii.bookiibookii.data.model.library.RelayReviewRequest
import com.bookiibookii.bookiibookii.data.model.library.ReviewRequest
import com.bookiibookii.bookiibookii.data.model.library.TrackerResponse
import com.bookiibookii.bookiibookii.data.model.library.UpdateCardRequest
import com.bookiibookii.bookiibookii.data.model.mypage.RelayReviewResponse
import com.bookiibookii.bookiibookii.data.model.user.PresignedUrlResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LibApi {

    // Library
    @GET("api/library/books")
    suspend fun getLibraryBooks(): Response<LibraryResponse>

    @DELETE("api/library/{userBookId}")
    suspend fun deleteGroup(
        @Path("userBookId") userBookId: Int
    ): Response<ApiResponse<String>>

    // Cards
    @GET("api/cards/detail/{cardId}")
    suspend fun getCardDetail(
        @Path("cardId") cardId: Long
    ): Response<CardDetailResponse>

    @GET("api/cards/{cardId}/comments")
    suspend fun getCardComments(
        @Path("cardId") cardId: Long
    ): Response<CommentListResponse>

    @GET("api/cards/group/{groupId}")
    suspend fun getGroupCards(
        @Path("groupId") groupId: Int
    ): Response<GroupCardListResponse>

    @POST("api/cards/{cardId}/comments")
    suspend fun postCardComment(
        @Path("cardId") cardId: Long,
        @Body request: PostCommentRequest
    ): Response<PostCommentResponse>

    @POST("api/cards/{userBookId}/presigned-url")
    suspend fun postPresignedUrl(
        @Path("userBookId") userBookId: Int
    ): Response<ApiResponse<PresignedUrlResult>>

    @POST("api/cards/{userBookId}")
    suspend fun createCard(
        @Path("userBookId") userBookId: Int,
        @Body request: CreateCardRequest
    ): Response<CreateCardResponse>

    @PATCH("api/cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: Long,
        @Body request: UpdateCardRequest
    ): Response<CardOperationResponse>

    @PATCH("api/cards/{cardId}/bookmark")
    suspend fun toggleBookmark(
        @Path("cardId") cardId: Long
    ): Response<BookmarkToggleResponse>

    @GET("api/cards/bookmarks")
    suspend fun getBookmarkedCards(): Response<BookmarkListResponse>

    @DELETE("api/cards/{cardId}")
    suspend fun deleteCard(
        @Path("cardId") cardId: Long
    ): Response<ApiResponse<String>>

    // Reviews
    @POST("api/reviews/together/{userBookId}")
    suspend fun postBookReview(
        @Path("userBookId") userBookId: Int,
        @Body request: ReviewRequest
    ): Response<ApiResponse<String>>

    @GET("/api/reviews/me/relay")
    suspend fun getRelayReviews(): Response<RelayReviewResponse>

    @POST("/api/reviews/relay/{userBookId}")
    suspend fun postRelayReview(
        @Path("userBookId") userBookId: Int,
        @Body request: RelayReviewRequest
    ): Response<ApiResponse<String>>

    @POST("/api/reviews/relay/{userBookId}/book")
    suspend fun postRelayBookReview(
        @Path("userBookId") userBookId: Int,
        @Body request: RelayBookReviewRequest
    ): Response<ApiResponse<String>>

    // 트래커 / 완독 (라이브러리에서 사용)
    @GET("/api/groups/me/trackers")
    suspend fun getMyTrackers(): Response<TrackerResponse>

    @PATCH("/api/groups/{groupId}/together/members/me/complete")
    suspend fun completeReading(
        @Path("groupId") groupId: Int
    ): Response<CompleteReadingResponse>
}
