package com.bookiibookii.bookiibookii.trkData.api

import com.bookiibookii.bookiibookii.trkData.dto.ApiResponse
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TrkApi {
    @GET("/api/groups/me/trackers/host")
    suspend fun getHostTrackers(): Response<ApiResponse<List<HostTrackerListItemDto>>>

    @GET("/api/groups/{groupId}/tracker")
    suspend fun getTrackerDetail(
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<TrackerDetailDto>>

    @POST("api/groups/{groupId}/tracker/images/presigned-url")
    suspend fun issuePresignedUrl(
        @Path("groupId") groupId: Long
    ): ApiResponse<PresignedUrlResponse>


    @POST("/api/groups/{groupId}/tracker/shipping")
    suspend fun startShipping(
        @Path("groupId") groupId: Long,
        @Body req: StartShippingRequest
    ): ApiResponse<TrackerDetailDto>
}

data class PresignedUrlRequest(
    val imageType: String,
    val contentType: String
)

data class PresignedUrlResponse(
    val presignedPutUrl: String,
    val s3Key: String
)

data class StartShippingRequest(
    val s3Key: String
)

