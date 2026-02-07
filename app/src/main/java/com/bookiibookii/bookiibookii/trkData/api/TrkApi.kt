package com.bookiibookii.bookiibookii.trkData.api

import com.bookiibookii.bookiibookii.trkData.dto.ApiResponse
import com.bookiibookii.bookiibookii.trkData.dto.GuestTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.HostTrackerListItemDto
import com.bookiibookii.bookiibookii.trkData.dto.TrackerDetailResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TrkApi {
    @GET("/api/groups/me/trackers/host")
    suspend fun getHostTrackers(): Response<ApiResponse<List<HostTrackerListItemDto>>>

    @GET("/api/groups/me/trackers/guest")
    suspend fun getGuestTrackers(): Response<ApiResponse<List<GuestTrackerListItemDto>>>

    @GET("/api/groups/{groupId}/tracker")
    suspend fun getTrackerDetail(
        @Path("groupId") groupId: Long
    ): ApiResponse<TrackerDetailResponseDto>
}


