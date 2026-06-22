package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.mypage.AddFavoriteBookRequest
import com.bookiibookii.bookiibookii.data.model.mypage.AddRepresentativeBookRequest
import com.bookiibookii.bookiibookii.data.model.mypage.BookshelfResult
import com.bookiibookii.bookiibookii.data.model.mypage.GroupMemberResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryCreateResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.mypage.MyGroupResponse
import com.bookiibookii.bookiibookii.data.model.mypage.MypageReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeDetailResponse
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReceivedReviews
import com.bookiibookii.bookiibookii.data.model.mypage.ReportCreateResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportRequest
import com.bookiibookii.bookiibookii.data.model.mypage.UpdateIntroductionReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.UpdateRepresentativeOrderRequest
import com.bookiibookii.bookiibookii.data.model.mypage.UserProfileResDTO
import com.bookiibookii.bookiibookii.data.model.mypage.WithdrawalReqDTO
import com.bookiibookii.bookiibookii.data.model.mypage.WrittenReviews
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MypApi {

    @GET("api/mypage")
    suspend fun getMypage(): Response<ApiResponse<UserProfileResDTO>>

    @GET("api/mypage/reviews/written")
    suspend fun getWrittenReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): Response<ApiResponse<WrittenReviews>>

    @GET("api/mypage/reviews/received")
    suspend fun getReceivedReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): Response<ApiResponse<ReceivedReviews>>

    @PATCH("api/mypage")
    suspend fun updateProfile(
        @Body request: MypageReqDTO,
    ): Response<ApiResponse<String>>

    @POST("api/users/me/withdrawal")
    suspend fun withdraw(
        @Body request: WithdrawalReqDTO,
    ): Response<ApiResponse<String>>

    @PATCH("api/mypage/introduction")
    suspend fun updateIntroduction(
        @Body request: UpdateIntroductionReqDTO,
    ): Response<ApiResponse<String>>

    @GET("api/mypage/bookshelf")
    suspend fun getBookshelf(): Response<ApiResponse<BookshelfResult>>

    @POST("api/mypage/bookshelf/representatives")
    suspend fun addRepresentativeBook(
        @Body request: AddRepresentativeBookRequest,
    ): Response<ApiResponse<String>>

    @DELETE("api/mypage/bookshelf/representatives/{userBookId}")
    suspend fun deleteRepresentativeBook(
        @Path("userBookId") userBookId: Long,
    ): Response<ApiResponse<String>>

    @PATCH("api/mypage/bookshelf/representatives/order")
    suspend fun reorderRepresentativeBooks(
        @Body request: UpdateRepresentativeOrderRequest,
    ): Response<ApiResponse<String>>

    @POST("api/mypage/bookshelf/favorites")
    suspend fun addFavoriteBook(
        @Body request: AddFavoriteBookRequest,
    ): Response<ApiResponse<String>>

    @DELETE("api/mypage/bookshelf/favorites/{userBookId}")
    suspend fun deleteFavoriteBook(
        @Path("userBookId") userBookId: Long,
    ): Response<ApiResponse<String>>

    @GET("api/notice")
    suspend fun getNoticeList(): Response<NoticeListResponse>

    @GET("api/notice/{noticeId}")
    suspend fun getNoticeDetail(
        @Path("noticeId") noticeId: Long,
    ): Response<NoticeDetailResponse>

    @GET("api/inquiry")
    suspend fun getInquiryList(): Response<InquiryListResponse>

    @POST("api/inquiry")
    suspend fun postInquiry(
        @Body request: InquiryRequest,
    ): Response<InquiryCreateResponse>

    @GET("api/report")
    suspend fun getReportList(): Response<ReportListResponse>

    @POST("api/report")
    suspend fun postReport(
        @Body request: ReportRequest,
    ): Response<ReportCreateResponse>

    @GET("api/report/groups/my")
    suspend fun getMyGroups(): Response<MyGroupResponse>

    @GET("api/report/{groupId}/members")
    suspend fun getGroupMembers(
        @Path("groupId") groupId: Long,
    ): Response<GroupMemberResponse>
}
