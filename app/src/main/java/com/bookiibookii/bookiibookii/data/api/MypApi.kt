package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.mypage.GroupMemberResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryCreateResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.InquiryRequest
import com.bookiibookii.bookiibookii.data.model.mypage.MyGroupResponse
import com.bookiibookii.bookiibookii.data.model.mypage.MypageResponse
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeDetailResponse
import com.bookiibookii.bookiibookii.data.model.mypage.NoticeListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportCreateResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportListResponse
import com.bookiibookii.bookiibookii.data.model.mypage.ReportRequest
import com.bookiibookii.bookiibookii.data.model.mypage.UserUpdateRequest
import com.bookiibookii.bookiibookii.data.model.mypage.UserUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MypApi {

    // 마이페이지
    @GET("api/mypage")
    suspend fun getMypage(): Response<MypageResponse>

    @PATCH("api/mypage")
    suspend fun updateProfile(
        @Body request: UserUpdateRequest
    ): Response<UserUpdateResponse>

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
}
