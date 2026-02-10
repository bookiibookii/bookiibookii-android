package com.bookiibookii.bookiibookii.trkData.api

import com.bookiibookii.bookiibookii.trkData.dto.ApiResponse
import com.bookiibookii.bookiibookii.trkData.dto.GuestTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.MakeMeetingRequest
import com.bookiibookii.bookiibookii.trkData.dto.PresignedUrlResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerCheckImageResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerCheckShippingImageResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDoneResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerExtensionResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerMeetingResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReadingStartResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReceiveRequestDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerReceiveResponseDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerShippingStartRequestDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerShippingStartResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TrkApi {
    @GET("/api/groups/me/trackers/host")
    suspend fun getHostTrackers(): Response<ApiResponse<List<HostTrackerListItemDto>>>

    @GET("/api/groups/me/trackers/guest")
    suspend fun getGuestTrackers(): Response<ApiResponse<List<GuestTrackerListItemDto>>>

    @GET("/api/groups/{groupId}/tracker")
    suspend fun getTrackerDetail(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDetailResponseDto>

    @POST("/api/groups/{groupId}/tracker/shipping")
    suspend fun postTrackerShippingStart(
        @Path("groupId") groupId: Long,
        @Body request: TrackerShippingStartRequestDto
    ): ApiResponse<TrackerShippingStartResponseDto>

    @POST("/api/groups/{groupId}/tracker/images/presigned-url")
    suspend fun getTrackerImagePresignedUrl(
        @Path("groupId") groupId: Long
    ): ApiResponse<PresignedUrlResponseDto>

    @PATCH("/api/groups/{groupId}/tracker/receive")
    suspend fun patchTrackerReceive(
        @Path("groupId") groupId: Long,
        @Body request: TrackerReceiveRequestDto
    ): ApiResponse<TrackerReceiveResponseDto>

    @PATCH("/api/groups/{groupId}/tracker/reading")
    suspend fun patchTrackerReadingStart(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerReadingStartResponseDto>

    @PATCH("/api/groups/{groupId}/tracker/extension")
    suspend fun patchTrackerExtension(
        @Path("groupId") groupId: Long,
        @Query("days") days: Int = 3
    ): ApiResponse<TrackerExtensionResponseDto>

    @PATCH("/api/groups/{groupId}/tracker/done")
    suspend fun patchTrackerDone(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDoneResponseDto>

    @GET("/api/groups/{groupId}/tracker/check/shipping")
    suspend fun getTrackerCheckShippingImage(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerCheckShippingImageResponseDto>

    @GET("/api/groups/{groupId}/tracker/check/received")
    suspend fun getTrackerCheckReceivedImage(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerCheckImageResponseDto>

    @PATCH("api/groups/{groupId}/tracker/makeMeeting")
    suspend fun makeMeeting(
        @Path("groupId") groupId: Long,
        @Body request: MakeMeetingRequest
    ): Response<TrackerDetailDto>

    @GET("/api/groups/{groupId}/tracker/meeting")
    suspend fun getTrackerMeeting(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerMeetingResponseDto>

    @PATCH("/api/groups/{groupId}/tracker/meeting/complete")
    suspend fun patchMeetingComplete(
        @Path("groupId") groupId: Long
    ): retrofit2.Response<TrackerDetailDto>

    @PATCH("/api/groups/{groupId}/tracker/confirm-reception")
    suspend fun patchConfirmReception(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDetailResponseDto>
}


