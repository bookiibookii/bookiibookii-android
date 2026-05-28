package com.bookiibookii.bookiibookii.data.api

import com.bookiibookii.bookiibookii.data.model.common.ApiResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerCompletionResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerListItemResDTO
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDeliveryRequest
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDeliveryResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerDetailResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerExtensionResponse
import com.bookiibookii.bookiibookii.data.model.tracker.TrackerImagePresignedUrlResponse
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
    suspend fun getMyTrackers(): Response<ApiResponse<List<TrackerListItemResDTO>>>

    @GET("/api/groups/{groupId}/tracker")
    suspend fun getTrackerDetail(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDetailResponse>

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
    ): Response<TrackerDetailResponse>

    @GET("/api/groups/{groupId}/tracker/meetings")
    suspend fun getTrackerMeeting(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerMeetingResponse>

    @PATCH("/api/groups/{groupId}/tracker/meetings/completion")
    suspend fun patchMeetingComplete(
        @Path("groupId") groupId: Long
    ): Response<TrackerDetailResponse>

    @PATCH("/api/groups/{groupId}/tracker/reception/verification")
    suspend fun patchConfirmReception(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDetailResponse>
}
