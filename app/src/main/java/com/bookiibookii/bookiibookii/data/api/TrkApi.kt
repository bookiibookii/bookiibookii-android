package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.GroupReviewsResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressDirectUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressSavedUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewCreateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.PartnerDeliveryResponseDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingPeriodUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingPeriodUpdateResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressResDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TrkApi {
    @GET("/api/me/trackers")
    suspend fun getMyTrackers(): Response<ApiResponse<TrackerListResDTO>>

    @GET("/api/trackers/{groupId}/tracker")
    suspend fun getTrackerDetail(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<TrackerDetailResDTO>>

    @PATCH("/api/trackers/{groupId}/reading-progress")
    suspend fun patchReadingProgress(
        @Path("groupId") groupId: Long,
        @Body request: ReadingProgressReqDTO,
    ): Response<ApiResponse<ReadingProgressResDTO>>

    // 독서 기간(예상 종료일) 수정 — 호스트 전용, MY_BOOK_READING/PARTNER_BOOK_READING 단계에서만
    @PATCH("/api/trackers/{groupId}/reading-period")
    suspend fun patchReadingPeriod(
        @Path("groupId") groupId: Long,
        @Body request: ReadingPeriodUpdateReqDTO,
    ): Response<ApiResponse<ReadingPeriodUpdateResDTO>>

    // 그룹 후기 전체 조회 — 기존 책 후기 프리필용(writerId로 내 후기 선별)
    @GET("/api/groups/{groupId}/reviews")
    suspend fun getGroupReviews(
        @Path("groupId") groupId: Long,
    ): Response<ApiResponse<GroupReviewsResDTO>>

    @POST("/api/groups/{groupId}/reviews")
    suspend fun postBookReview(
        @Path("groupId") groupId: Long,
        @Body request: BookReviewReqDTO,
    ): Response<ApiResponse<BookReviewResDTO>>

    // 내 책 리뷰 수정
    @PATCH("/api/groups/{groupId}/reviews/me")
    suspend fun patchMyBookReview(
        @Path("groupId") groupId: Long,
        @Body request: BookReviewReqDTO,
    ): Response<ApiResponse<BookReviewResDTO>>

    @POST("/api/groups/{groupId}/member-reviews")
    suspend fun postMemberReview(
        @Path("groupId") groupId: Long,
        @Body request: MemberReviewCreateReqDTO,
    ): Response<ApiResponse<MemberReviewResDTO>>

    @POST("/api/groups/{groupId}/deliveries")
    suspend fun postDeliveryRegister(
        @Path("groupId") groupId: Long,
        @Body request: DeliveryRegisterReqDTO,
    ): Response<ApiResponse<String>>

    @POST("/api/groups/{groupId}/meetings")
    suspend fun postMeetingRegister(
        @Path("groupId") groupId: Long,
        @Body request: MeetingRegisterReqDTO,
    ): Response<ApiResponse<MeetingResDTO>>

    @GET("/api/groups/{groupId}/meetings")
    suspend fun getMeeting(
        @Path("groupId") groupId: Long,
    ): Response<ApiResponse<MeetingResDTO>>

    @PATCH("/api/groups/{groupId}/meetings/completion")
    suspend fun patchMeetingCompletion(
        @Path("groupId") groupId: Long,
    ): Response<ApiResponse<MeetingResDTO>>

    @GET("/api/groups/{groupId}/deliveries/address")
    suspend fun getDeliveryAddress(
        @Path("groupId") groupId: Long,
    ): Response<ApiResponse<DeliveryAddressResDTO>>

    // 이번 교환 배송지 변경 - 기존(마이페이지 등록) 배송지 선택
    @PUT("/api/groups/{groupId}/deliveries/address/me/saved")
    suspend fun putMyDeliveryAddressSaved(
        @Path("groupId") groupId: Long,
        @Body request: DeliveryAddressSavedUpdateReqDTO,
    ): Response<ApiResponse<DeliveryAddressResDTO>>

    // 이번 교환 배송지 변경 - 직접 입력
    @PUT("/api/groups/{groupId}/deliveries/address/me/direct")
    suspend fun putMyDeliveryAddressDirect(
        @Path("groupId") groupId: Long,
        @Body request: DeliveryAddressDirectUpdateReqDTO,
    ): Response<ApiResponse<DeliveryAddressResDTO>>

    // 상대방이 나에게 보낸 운송장 정보 조회
    @GET("/api/groups/{groupId}/deliveries/partner")
    suspend fun getPartnerDelivery(
        @Path("groupId") groupId: Long,
    ): Response<ApiResponse<PartnerDeliveryResponseDTO>>

    // 상대방 운송장 수령 확인
    @PATCH("/api/groups/{groupId}/deliveries/partner/receive")
    suspend fun patchPartnerReceive(
        @Path("groupId") groupId: Long,
    ): Response<ApiResponse<String>>
}
