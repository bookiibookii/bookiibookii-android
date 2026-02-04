package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.InquiryCreateResponse
import com.bookiibookii.bookiibookii.data.model.InquiryListResponse
import com.bookiibookii.bookiibookii.data.model.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.LoginRequest
import com.bookiibookii.bookiibookii.data.model.LoginResponse
import com.bookiibookii.bookiibookii.data.model.LogoutResponse
import com.bookiibookii.bookiibookii.data.model.MypageResponse
import com.bookiibookii.bookiibookii.data.model.NoticeDetailResponse
import com.bookiibookii.bookiibookii.data.model.NoticeListResponse
import com.bookiibookii.bookiibookii.data.model.ReportCreateResponse
import com.bookiibookii.bookiibookii.data.model.ReportListResponse
import com.bookiibookii.bookiibookii.data.model.ReportRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateRequest
import com.bookiibookii.bookiibookii.data.model.UserUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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
    suspend fun withdraw(): Response<LogoutResponse>

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
}


