package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.mypage.GroupMemberResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryCreateResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.LogoutResponse
import com.bookiibookii.bookiibookii.data.model.mypage.MyGroupResponse
import com.bookiibookii.bookiibookii.data.model.mypage.MypageResponse
import com.bookiibookii.bookiibookii.data.model.NicknameValidationResponse
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeDetailResponse
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeListResponse
import com.bookiibookii.bookiibookii.data.model.OtherProfileResponse
import com.bookiibookii.bookiibookii.data.model.PresignedUrlResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportCreateResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportRequest
import com.bookiibookii.bookiibookii.data.model.mypage.UserUpdateRequest
import com.bookiibookii.bookiibookii.data.model.mypage.UserUpdateResponse
import com.bookiibookii.bookiibookii.data.model.WithdrawResponse
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

interface MypApi {

    // 마이페이지
    @GET("api/mypage")
    suspend fun getMypage(): Response<MypageResponse>

    @PATCH("api/mypage")
    suspend fun updateProfile(
        @Body request: UserUpdateRequest
    ): Response<UserUpdateResponse>

    // 로그아웃
    @POST("api/auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    // 회원탈퇴
    @DELETE("api/auth/withdraw")
    suspend fun withdraw(): Response<WithdrawResponse>

    // Notice
    @GET("api/notice")
    suspend fun getNoticeList(): Response<NoticeListResponse>

    @GET("api/notice/{noticeId}")
    suspend fun getNoticeDetail(
        @Path("noticeId") noticeId: Int
    ): Response<NoticeDetailResponse>

    // Inquiry
    @GET("api/inquiry")
    suspend fun getInquiryList(): Response<InquiryListResponse>

    @POST("api/inquiry")
    suspend fun postInquiry(@Body request: InquiryRequest): Response<InquiryCreateResponse>

    // Report
    @GET("api/report")
    suspend fun getReportList(): Response<ReportListResponse>

    @POST("api/report")
    suspend fun postReport(@Body request: ReportRequest): Response<ReportCreateResponse>

    @GET("api/report/groups/my")
    suspend fun getMyGroups(): Response<MyGroupResponse>

    @GET("api/report/{groupId}/members")
    suspend fun getGroupMembers(
        @Path("groupId") groupId: Int
    ): Response<GroupMemberResponse>

    // 프로필 이미지 업로드
    @POST("api/users/me/image/presigend-url")
    suspend fun getPresignedUrl(): Response<PresignedUrlResponse>

    @PUT
    suspend fun uploadImageToS3(
        @Url url: String,
        @Body image: RequestBody
    ): Response<Unit>

    @POST("api/users/name-validation")
    suspend fun postNicknameValidation(
        @Query("nickname") nickname: String
    ): Response<NicknameValidationResponse>

    @POST("api/users/me/image/presigned-url")
    suspend fun postPresignedUrl(): Response<PresignedUrlResponse>

    // 다른 사용자 프로필
    @GET("/api/profiles/{nickname}")
    suspend fun getUserProfile(
        @Path("nickname") nickname: String
    ): Response<OtherProfileResponse>
}
