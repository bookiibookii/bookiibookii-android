package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.mypage.AddFavoriteBookRequest
import com.bookiibookii.bookiibookii.data.model.mypage.AddRepresentativeBookRequest
import com.bookiibookii.bookiibookii.data.model.mypage.BookshelfResult
import com.bookiibookii.bookiibookii.data.model.mypage.GroupMemberResponse
import com.bookiibookii.bookiibookii.data.model.mypage.FaqListResponse
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
import com.bookiibookii.bookiibookii.data.model.mypage.ProfileShareTokenResponseDTO
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

    // 마이페이지 조회
    @GET("api/mypage")
    suspend fun getMypage(): Response<ApiResponse<UserProfileResDTO>>

    // 작성한 후기 조회(페이징)
    @GET("api/mypage/reviews/written")
    suspend fun getWrittenReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): Response<ApiResponse<WrittenReviews>>

    // 받은 후기 조회(페이징)
    @GET("api/mypage/reviews/received")
    suspend fun getReceivedReviews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
    ): Response<ApiResponse<ReceivedReviews>>

    // 마이페이지 정보 수정 (닉네임, 성별, 생년월일, 프로필 이미지)
    @PATCH("api/mypage")
    suspend fun updateProfile(
        @Body request: MypageReqDTO,
    ): Response<ApiResponse<String>>

    // 회원탈퇴
    @POST("api/users/me/withdrawal")
    suspend fun withdraw(
        @Body request: WithdrawalReqDTO,
    ): Response<ApiResponse<String>>

    // 한줄 소개 수정
    @PATCH("api/mypage/introduction")
    suspend fun updateIntroduction(
        @Body request: UpdateIntroductionReqDTO,
    ): Response<ApiResponse<String>>

    // 나의 책장 조회
    @GET("api/mypage/bookshelf")
    suspend fun getBookshelf(): Response<ApiResponse<BookshelfResult>>

    // 대표책 등록
    @POST("api/mypage/bookshelf/representatives")
    suspend fun addRepresentativeBook(
        @Body request: AddRepresentativeBookRequest,
    ): Response<ApiResponse<String>>

    // 대표책 삭제
    @DELETE("api/mypage/bookshelf/representatives/{userBookId}")
    suspend fun deleteRepresentativeBook(
        @Path("userBookId") userBookId: Long,
    ): Response<ApiResponse<String>>

    // 대표책 순서 변경
    @PATCH("api/mypage/bookshelf/representatives/order")
    suspend fun reorderRepresentativeBooks(
        @Body request: UpdateRepresentativeOrderRequest,
    ): Response<ApiResponse<String>>

    // 인생 책 등록
    @POST("api/mypage/bookshelf/favorites")
    suspend fun addFavoriteBook(
        @Body request: AddFavoriteBookRequest,
    ): Response<ApiResponse<String>>

    // 인생 책 삭제
    @DELETE("api/mypage/bookshelf/favorites/{userBookId}")
    suspend fun deleteFavoriteBook(
        @Path("userBookId") userBookId: Long,
    ): Response<ApiResponse<String>>

    // 인생 책 교체
    @PATCH("api/mypage/bookshelf/favorites/{userBookId}")
    suspend fun replaceFavoriteBook(
        @Path("userBookId") userBookId: Long,
        @Body request: AddFavoriteBookRequest,
    ): Response<ApiResponse<String>>

    // 프로필 공유 토큰 발급
    @POST("api/mypage/share-token")
    suspend fun createProfileShareToken(): Response<ApiResponse<ProfileShareTokenResponseDTO>>

    // 공지 목록 조회
    @GET("api/notice")
    suspend fun getNoticeList(): Response<NoticeListResponse>

    // 공지 상세 조회
    @GET("api/notice/{noticeId}")
    suspend fun getNoticeDetail(
        @Path("noticeId") noticeId: Long,
    ): Response<NoticeDetailResponse>

    // 자주 묻는 질문 조회
    @GET("api/faq")
    suspend fun getFaq(): Response<FaqListResponse>

    // 문의 목록 조회
    @GET("api/inquiry")
    suspend fun getInquiryList(): Response<InquiryListResponse>

    // 문의 작성
    @POST("api/inquiry")
    suspend fun postInquiry(
        @Body request: InquiryRequest,
    ): Response<InquiryCreateResponse>

    // 신고 목록 조회
    @GET("api/report")
    suspend fun getReportList(): Response<ReportListResponse>

    // 신고 작성
    @POST("api/report")
    suspend fun postReport(
        @Body request: ReportRequest,
    ): Response<ReportCreateResponse>

    // 신고할 그룹 조회
    @GET("api/report/groups/my")
    suspend fun getMyGroups(): Response<MyGroupResponse>

    // 신고할 그룹의 멤버 조회
    @GET("api/report/{groupId}/members")
    suspend fun getGroupMembers(
        @Path("groupId") groupId: Long,
    ): Response<GroupMemberResponse>
}
