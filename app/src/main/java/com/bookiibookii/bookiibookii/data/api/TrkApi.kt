package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerCompletionResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDeliveryRequest
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDeliveryResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerExtensionResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerImagePresignedUrlResponse
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.BookReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryAddressUpdateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.DeliveryRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingRegisterReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MeetingResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewCreateReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.PartnerDeliveryResponseDTO
import com.bookiibookii.bookiibookii.data.model.tracker.MemberReviewResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressReqDTO
import com.bookiibookii.bookiibookii.data.model.tracker.ReadingProgressResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerMeetingRequest
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerMeetingResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerReadingResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerReceptionImageResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerReceptionRequest
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerReceptionResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerShippingImageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @PATCH("/api/groups/{groupId}/deliveries/address/me")
    suspend fun patchMyDeliveryAddress(
        @Path("groupId") groupId: Long,
        @Body request: DeliveryAddressUpdateReqDTO,
    ): Response<ApiResponse<String>>

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

    @POST("/api/groups/{groupId}/tracker/delivery")
    suspend fun postTrackerShippingStart(
        @Path("groupId") groupId: Long,
        @Body request: TrackerDeliveryRequest
    ): ApiResponse<TrackerDeliveryResponse>

    @POST("/api/groups/{groupId}/tracker/images/presigned-url")
    suspend fun getTrackerImagePresignedUrl(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerImagePresignedUrlResponse>

    @PATCH("/api/groups/{groupId}/tracker/reception")
    suspend fun patchTrackerReceive(
        @Path("groupId") groupId: Long,
        @Body request: TrackerReceptionRequest
    ): ApiResponse<TrackerReceptionResponse>

    @PATCH("/api/groups/{groupId}/tracker/reading")
    suspend fun patchTrackerReadingStart(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerReadingResponse>

    @PATCH("/api/groups/{groupId}/tracker/extension")
    suspend fun patchTrackerExtension(
        @Path("groupId") groupId: Long,
        @Query("days") days: Int = 3
    ): ApiResponse<TrackerExtensionResponse>

    @PATCH("/api/groups/{groupId}/tracker/done")
    suspend fun patchTrackerDone(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerCompletionResponse>

    @GET("/api/groups/{groupId}/tracker/images/delivery")
    suspend fun getTrackerCheckShippingImage(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerShippingImageResponse>

    @GET("/api/groups/{groupId}/tracker/images/received")
    suspend fun getTrackerCheckReceivedImage(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerReceptionImageResponse>

    @PATCH("api/groups/{groupId}/tracker/meetings")
    suspend fun makeMeeting(
        @Path("groupId") groupId: Long,
        @Body request: TrackerMeetingRequest
    ): Response<TrackerDetailResDTO>

    @GET("/api/groups/{groupId}/tracker/meetings")
    suspend fun getTrackerMeeting(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerMeetingResponse>

    @PATCH("/api/groups/{groupId}/tracker/meetings/completion")
    suspend fun patchMeetingComplete(
        @Path("groupId") groupId: Long
    ): Response<TrackerDetailResDTO>

    @PATCH("/api/groups/{groupId}/tracker/reception/verification")
    suspend fun patchConfirmReception(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDetailResDTO>
}
